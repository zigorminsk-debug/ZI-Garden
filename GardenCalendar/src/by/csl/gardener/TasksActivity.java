package by.csl.gardener;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class TasksActivity extends Activity {
    @Override
    protected void attachBaseContext(android.content.Context context) {
        super.attachBaseContext(Ui.applyFont(context));
    }

    private LinearLayout container;
    private boolean onlyPending = true;
    private Storage store;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.store = new Storage(this);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f), Ui.dp(this, 12.0f));
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText("Показывать только невыполненные");
        checkBox.setChecked(true);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                TasksActivity.this.m26lambda$onCreate$0$bycslgardenerTasksActivity(compoundButton, z);
            }
        });
        linearLayout.addView(checkBox);
        LinearLayout linearLayout2 = new LinearLayout(this);
        this.container = linearLayout2;
        linearLayout2.setOrientation(1);
        linearLayout.addView(this.container);
        scrollView.addView(linearLayout);
        Ui.setContent(this, scrollView);
        render();
    }

    void m26lambda$onCreate$0$bycslgardenerTasksActivity(CompoundButton compoundButton, boolean z) {
        this.onlyPending = z;
        render();
    }

    public void render() {
        this.container.removeAllViews();
        Storage storage = this.store;
        int i = 0;
        for (Task task : new Planner(storage, Weather.fromJson(storage.weatherCache())).tasks(21)) {
            if (!this.onlyPending || !task.done) {
                if (!Dates.at(task.year, task.month, task.day).before(Dates.today())) {
                    Ui.section(this.container, this, Dates.fmt(task.year, task.month, task.day) + ", " + Dates.weekday(task.year, task.month, task.day));
                    this.container.addView(Ui.taskCard(this, task, new Runnable() {
                        public final void run() {
                            TasksActivity.this.render();
                        }
                    }));
                    i++;
                }
            }
        }
        if (i == 0) {
            this.container.addView(Ui.text(this, "Нет работ в выбранных условиях.", 14.0f, getResources().getColor(R.color.text_sub), false));
        }
    }
}
