package by.csl.gardener;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import java.util.Iterator;
import java.util.List;

/** Определение координат: сначала последняя известная точка, затем GPS/сеть. */
public final class Geo {

    public static final int REQ_GPS = 77;

    private Geo() {
    }

    /** Обратный вызов результата: широта, долгота, подпись места. */
    public interface OnFix {
        void onFixed(double lat, double lon, String label);
    }

    public static boolean hasPermission(Activity activity) {
        return activity.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED
                || activity.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(Activity activity) {
        activity.requestPermissions(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_GPS);
    }

    public static void locate(final Activity activity, final OnFix cb) {
        LocationManager lm = (LocationManager) activity.getSystemService("location");
        if (lm == null) {
            Ui.toast(activity, "GPS недоступен на этом устройстве");
            return;
        }
        Location best = null;
        try {
            Iterator<String> it = lm.getAllProviders().iterator();
            while (it.hasNext()) {
                Location last = lm.getLastKnownLocation(it.next());
                if (last != null && (best == null || last.getTime() > best.getTime())) {
                    best = last;
                }
            }
        } catch (Exception unused) {
            best = null;
        }
        if (best != null) {
            cb.onFixed(round4(best.getLatitude()), round4(best.getLongitude()),
                    label(activity, best.getLatitude(), best.getLongitude()));
            return;
        }
        try {
            String provider = lm.isProviderEnabled("gps") ? "gps" : "network";
            Ui.toast(activity, "Определяем координаты по GPS…");
            lm.requestSingleUpdate(provider, new LocationListener() {
                @Override
                public void onLocationChanged(final Location location) {
                    if (location == null) return;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cb.onFixed(round4(location.getLatitude()),
                                    round4(location.getLongitude()),
                                    label(activity, location.getLatitude(), location.getLongitude()));
                        }
                    });
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            }, Looper.getMainLooper());
        } catch (Exception e) {
            Ui.toast(activity, "GPS не дал координаты: " + e.getMessage());
        }
    }

    public static String label(Activity activity, double lat, double lon) {
        try {
            List<Address> found = new Geocoder(activity).getFromLocation(lat, lon, 1);
            if (found == null || found.isEmpty()) {
                return "Мой участок (GPS)";
            }
            Address a = found.get(0);
            String locality = a.getLocality() != null ? a.getLocality() : a.getAdminArea();
            if (locality == null || locality.isEmpty()) {
                return "Мой участок (GPS)";
            }
            return locality;
        } catch (Exception unused) {
            return "Мой участок (GPS)";
        }
    }

    public static double round4(double d) {
        return Math.round(d * 10000.0d) / 10000.0d;
    }
}
