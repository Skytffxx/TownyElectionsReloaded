package me.skyttffxx.townyelectionsreloaded.data;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DataHandler {

        private static Gson gson;
        private File _dataFile;
        private boolean _dataIsLoaded;
        private JSONObject _jsonData;
        private JSONObject _jsonDataToSave;

        /**
         * @param dataFolder Folder where the data file will be loaded/stored
         * @param fileName   Name of the data file
         */
        public DataHandler(File dataFolder, String fileName) {

                gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

                _jsonDataToSave = new JSONObject();
                _dataFile = new File(dataFolder, fileName);

                if (!dataFolder.exists()) return;

                _dataIsLoaded = _dataFile.exists();

                if (!_dataIsLoaded) return;

                JSONParser parser = new JSONParser();

                Object obj;
                try {
                        obj = parser.parse(new FileReader(_dataFile));
                        _jsonData = (JSONObject) obj;
                } catch (IOException | ParseException e) { e.printStackTrace(); }

        }

        /**
         * @param dataTag Tag of the JSONObject list you want to retrieve from the data file
         * @return JSONObject list under the tag specified
         */
        public List<JSONObject> getDataList(String dataTag) {
                if (!_dataIsLoaded) return null;
                if (!_jsonData.containsKey(dataTag)) return null;
                JSONArray jsonArray = (JSONArray) _jsonData.get(dataTag);
                List<JSONObject> dataList = new ArrayList<JSONObject>();
                for (int i = 0; i < jsonArray.size(); i++) {
                        dataList.add((JSONObject) jsonArray.get(i));
                }
                return dataList;
        }

        /**
         * @param dataTag Tag of the plain JSONObject to retrieve
         * @return JSONObject under the tag, or null if not present
         */
        public JSONObject getDataObject(String dataTag) {
                if (!_dataIsLoaded) return null;
                if (_jsonData == null) return null;
                if (!_jsonData.containsKey(dataTag)) return null;
                Object val = _jsonData.get(dataTag);
                if (val instanceof JSONObject) return (JSONObject) val;
                return null;
        }

        /**
         * @param dataTag   Tag under which to store the data list
         * @param dataArray List of JSONObject to store
         */
        @SuppressWarnings("unchecked")
        public void addDataList(String dataTag, List<JSONObject> dataArray) {
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < dataArray.size(); i++) {
                        jsonArray.add(dataArray.get(i));
                }
                if (_jsonDataToSave.containsKey(dataTag)) _jsonDataToSave.remove(dataTag);
                _jsonDataToSave.put(dataTag, jsonArray);
        }

        /**
         * @param dataTag Tag under which to store a plain JSONObject
         * @param obj     JSONObject to store
         */
        @SuppressWarnings("unchecked")
        public void addDataObject(String dataTag, JSONObject obj) {
                if (_jsonDataToSave.containsKey(dataTag)) _jsonDataToSave.remove(dataTag);
                _jsonDataToSave.put(dataTag, obj);
        }

        /**
         * Save data to the data file
         */
        public void saveData() {
                try {
                        if (!_dataFile.getParentFile().exists()) {
                                _dataFile.getParentFile().mkdirs();
                        }
                        try (FileWriter fw = new FileWriter(_dataFile)) {
                                fw.write(_jsonDataToSave.toString());
                        }
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public static Gson getGson() {
                return gson;
        }

}
