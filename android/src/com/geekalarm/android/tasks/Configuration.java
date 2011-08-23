package com.geekalarm.android.tasks;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

/**
 * Configuration is map of all categories.
 */
public class Configuration {

    public static final int DEFAULT_DIFFICULTY = 2;

    private Map<Category, Integer> categories;

    public Map<Category, Integer> getCategories() {
        return categories;
    }

    public void setCategories(Map<Category, Integer> categories) {
        this.categories = Collections.unmodifiableMap(categories);
    }
    
    public static Configuration getConfiguration(int difficulty) {
        try {
            List<Category> categories = TaskManager.getCategories();
            Map<Category, Integer> catMap = new HashMap<Category, Integer>();
            for (Category category : categories) {
                catMap.put(category, difficulty);
            }
            Configuration conf = new Configuration();
            conf.setCategories(catMap);
            return conf;
        } catch (Exception e) {
            Log.e(Configuration.class.getName(), "Something bad", e);
            return null;
        }
    }

    public static Configuration getDefaultConfiguration() {
        return getConfiguration(DEFAULT_DIFFICULTY);
    }
}
