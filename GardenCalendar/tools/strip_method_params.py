#!/usr/bin/env python3
"""
Удаляет атрибут MethodParameters из .class файлов.

Зачем: javac из JDK 21 при компиляции анонимных внутренних классов записывает
MethodParameters с name_index = 0 (без имени) для mandated/synthetic параметров.
d8 из Android build-tools 34.0.0 на таких атрибутах падает:
  java.lang.NullPointerException: Cannot invoke "String.length()" because "<parameter1>" is null
Атрибут отладочный и на работу приложения не влияет.
"""
import struct
import sys

MAGIC = 0xCAFEBABE


class Reader:
    def __init__(self, data):
        self.d = data
        self.i = 0

    def u1(self):
        v = self.d[self.i]
        self.i += 1
        return v

    def u2(self):
        v = struct.unpack_from('>H', self.d, self.i)[0]
        self.i += 2
        return v

    def u4(self):
        v = struct.unpack_from('>I', self.d, self.i)[0]
        self.i += 4
        return v

    def take(self, n):
        v = self.d[self.i:self.i + n]
        self.i += n
        return v


def constant_sizes(r, count):
    """Возвращает список (index, размер_в_байтах) и словарь utf8 {index: str}."""
    utf8 = {}
    i = 1
    while i < count:
        tag = r.u1()
        if tag == 1:
            ln = r.u2()
            utf8[i] = r.take(ln).decode('utf-8', 'replace')
        elif tag in (7, 8, 16, 19, 20):
            r.u2()
        elif tag in (15,):
            r.take(3)
        elif tag in (5, 6):
            r.take(8)
            i += 1  # long/double занимают две ячейки
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
            r.take(4)
        else:
            raise ValueError('unknown constant tag %d at index %d' % (tag, i))
        i += 1
    return utf8


def process(path):
    with open(path, 'rb') as f:
        data = f.read()
    r = Reader(data)
    if r.u4() != MAGIC:
        return False
    r.u2(); r.u2()                      # minor, major
    cp_count = r.u2()
    utf8 = constant_sizes(r, cp_count)
    r.u2()                              # access flags
    r.u2(); r.u2()                      # this, super
    for _ in range(r.u2()):             # interfaces
        r.u2()
    # fields
    for _ in range(r.u2()):
        r.u2(); r.u2(); r.u2()
        for _ in range(r.u2()):
            r.u2(); r.take(r.u4())
    # methods — здесь и вырезаем MethodParameters
    out = bytearray(data[:r.i])
    changed = False
    method_count = r.u2()
    out += struct.pack('>H', method_count)
    for _ in range(method_count):
        out += r.take(6)                # access, name_index, descriptor_index
        attr_count = r.u2()
        kept = []
        for _ in range(attr_count):
            name_index = r.u2()
            length = r.u4()
            body = r.take(length)
            if utf8.get(name_index) == 'MethodParameters':
                changed = True
                continue
            kept.append((name_index, body))
        out += struct.pack('>H', len(kept))
        for name_index, body in kept:
            out += struct.pack('>HI', name_index, len(body))
            out += body
    out += data[r.i:]                   # class attributes + хвост
    if changed:
        with open(path, 'wb') as f:
            f.write(bytes(out))
    return changed


def main(argv):
    changed = 0
    for p in argv[1:]:
        try:
            if process(p):
                changed += 1
        except Exception as e:
            print('WARN: %s: %s' % (p, e), file=sys.stderr)
    print('MethodParameters stripped from %d class file(s)' % changed)
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
