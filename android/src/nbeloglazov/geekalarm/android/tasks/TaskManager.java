package nbeloglazov.geekalarm.android.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import nbeloglazov.geekalarm.android.R;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public final class TaskManager {

    private static final String SERVER_URL = "http://7133305a.dotcloud.com/";

    private TaskManager() {
    }

    public static Task getTestTask(Resources resources) {
        Task task = new Task();
        task.setQuestion(BitmapFactory.decodeResource(resources,
                R.drawable.question_test));
        int choicesIds[] = { R.drawable.choice1_test, R.drawable.choice2_test,
                R.drawable.choice3_test, R.drawable.choice4_test };
        Bitmap choices[] = new Bitmap[4];
        for (int i = 0; i < 4; i++) {
            choices[i] = BitmapFactory.decodeResource(resources, choicesIds[i]);
        }
        task.setChoices(choices);
        task.setCorrect(4);
        return task;
    }

    private static Category jsonToCategory(JSONObject json)
            throws JSONException {
        return new Category(json.getString("code"), json.getString("name"));
    }

    private static HttpEntity sendRequest(String url) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpUriRequest request = new HttpGet(SERVER_URL + url);
        HttpResponse response = client.execute(request);
        return response.getEntity();
    }

    public static List<Category> getCategories() throws Exception {
        String jsonText = EntityUtils.toString(sendRequest("categories"));
        JSONArray array = new JSONArray(jsonText);
        List<Category> categories = new ArrayList<Category>(array.length());
        for (int i = 0; i < array.length(); i++) {
            categories.add(jsonToCategory(array.getJSONObject(i)));
        }
        return categories;
    }

    private static Bitmap getImage(String url) throws Exception {
        HttpEntity entity = sendRequest(url);
        return BitmapFactory.decodeStream(entity.getContent());
    }

    public static Task getTask(Category category, int level) throws Exception {
        String url = String.format("task?category=%s&level=%d",
                category.getCode(), level);
        HttpEntity taskEntity = sendRequest(url);
        JSONObject taskJson = new JSONObject(EntityUtils.toString(taskEntity));
        String id = taskJson.getString("id");
        Task task = new Task();
        task.setId(id);
        task.setCorrect(Integer.parseInt(taskJson.getString("correct")));
        String questionUrl = String.format("image?id=%s&type=question", id);
        task.setQuestion(getImage(questionUrl));
        for (int i = 0; i < 4; i++) {
            String choiceUrl = String.format(
                    "image?id=%s&type=choice&number=%d", id, i + 1);
            task.setChoice(i, getImage(choiceUrl));
        }
        return task;
    }

    public static void addResult(String id, boolean solved) throws Exception {
        sendRequest(String.format("result?id=%s&solved=%s", id, solved));
    }
}
