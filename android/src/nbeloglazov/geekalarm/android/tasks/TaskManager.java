package nbeloglazov.geekalarm.android.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public final class TaskManager {

    private static final String SERVER_URL = "http://7133305a.dotcloud.com/";
    private static final Random RANDOM = new Random();

    private TaskManager() {
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
        if (id != null) {
            sendRequest(String.format("result?id=%s&solved=%s", id, solved));
        }
    }

    private static Bitmap getBitmapWithText(String text) {
        float size = 30;
        Paint paint = new Paint();
        paint.setTextSize(size);
        float width = paint.measureText(text);
        float height = paint.getTextSize();
        Bitmap bitmap = Bitmap.createBitmap((int) width + 10,
                (int) height + 10, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRGB(255, 255, 255);
        paint.setColor(Color.BLACK);
        canvas.drawText(text, 0, size, paint);
        return bitmap;
    }

    private static int[] getSimilarNumbers(int number) {
        Random rand = new Random();
        int[] res = new int[4];
        res[0] = number;
        int unit = RANDOM.nextInt(2) * 2 - 1;
        int ten = (RANDOM.nextInt(2) * 2 - 1) * 10;
        res[1] = number + unit;
        res[2] = number + ten;
        res[3] = number + unit + ten;
        return res;
    }

    private static int shuffle(int[] numbers) {
        int pos = 0;
        for (int i = 0; i < 4; i++) {
            int a = RANDOM.nextInt(4);
            int b = RANDOM.nextInt(4);
            pos = a == pos ? b : (b == pos ? a : pos);
            int tmp = numbers[a];
            numbers[a] = numbers[b];
            numbers[b] = tmp;
        }
        return pos;
    }

    private static Task generateTaskByData(String question, int answer) {
        int[] choices = getSimilarNumbers(answer);
        int correct = shuffle(choices);
        Bitmap[] choicesBitMap = new Bitmap[4];
        for (int i = 0; i < 4; i++) {
            String text = String.valueOf(choices[i]);
            choicesBitMap[i] = getBitmapWithText(text);
        }
        Task task = new Task();
        task.setQuestion(getBitmapWithText(question));
        task.setChoices(choicesBitMap);
        task.setCorrect(correct + 1);
        return task;
    }

    private static Task generateEasySimpleTask() {
        int a = RANDOM.nextInt(100);
        int b = RANDOM.nextInt(100);
        String question = String.format("%d + %d = ?", a, b);
        return generateTaskByData(question, a + b);
    }

    private static Task generateMediumSimpleTask() {
        int a = RANDOM.nextInt(100);
        int b = RANDOM.nextInt(100);
        String question = String.format("%d * %d = ?", a, b);
        return generateTaskByData(question, a * b);
    }

    private static Task generateHardSimpleTask() {
        int a = RANDOM.nextInt(100);
        int b = RANDOM.nextInt(100);
        int c = RANDOM.nextInt(100);
        String question = String.format("%1$d * %2$d + %3$d = ?", a, b, c);
        return generateTaskByData(question, a * b + c);
    }

    public static Task generateSimpleTask(int difficulty) {
        switch (difficulty) {
        case 1:
            return generateEasySimpleTask();
        case 2:
            return generateMediumSimpleTask();
        case 3:
            return generateHardSimpleTask();
        }
        return null;
    }
}
