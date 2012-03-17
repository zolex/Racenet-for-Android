package racenet.racenet;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class News extends Activity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);        
        this.setContentView(R.layout.news);
        
        Database db = new Database(getApplicationContext());
        String[] news = db.getLatestNews();
        
        TextView title = (TextView)findViewById(R.id.title);
        title.setText(news[0]);
        
        TextView body = (TextView)findViewById(R.id.body);
        body.setText(news[1]);
        
        db.clearNews();
	}
}
