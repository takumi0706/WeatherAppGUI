import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.xml.stream.Location;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

/*APIから天気データを取得。このバックエンドは外部APIから最新の天気データを取得し、GUIに返す。
GUIはこのデータをユーザーに表示する*/
public class WeatherApp {
    //指定した場所の天気データを取得
    public static JSONObject getWeatherData(String locationName) {
        //ジオロケーションAPIから位置座標を取得
        JSONArray locationData = getLocationData(locationName);

        //経緯と緯度を抽出
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        //位置情報付きのURLでリクエストしてAPIを作る
        String urlString = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude + "&longitude=" + longitude + "&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=Asia%2FTokyo";

        try {
            //APIを呼んで情報を取得
            HttpURLConnection conn = fetchApiResponse(urlString);

            //レスポンスコードを確認
            //200ならおｋ
            if (conn.getResponseCode() != 200) {
                System.out.println("エラー：APIに接続できませんでした。");
                return null;
            }
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNext()) {
                //string builderから読み込みと保存
                resultJson.append(scanner.nextLine());
            }

            //scannerを閉じる
            scanner.close();

//          urlの接続を切る
            conn.disconnect();

            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

//          一時間ごとのデータを取得
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");

            /*今の時間のデータが欲しいから今の時間のインデックスが欲しい*/
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            //温度を取得
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            //天気コードを取得
            JSONArray weathercode = (JSONArray) hourly.get("weather_code");
            String weatherCondition = convertWeatherCode((long) weathercode.get(index));

            //湿度を取得
            JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
            long humidity = (long) relativeHumidity.get(index);

//            風速を取得
            JSONArray windspeedData = (JSONArray) hourly.get("wind_speed_10m");
            double windSpeed = (double) windspeedData.get(index);

//            フロントエンドでアクセスする天気のjsonデータのオブジェクトを構築する
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windSpeed", windSpeed);

            return weatherData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONArray getLocationData(String locationName) {
        //APIのフォーマットに合わせる
        locationName = locationName.replaceAll(" ", "+");

        //位置の情報が入ったAPI urlを作る
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" + locationName + "&count=10&language=en&format=json";
        try {
            //APIを読んで、情報を得る
            HttpURLConnection conn = fetchApiResponse(urlString);

            //レスポンス状態をチェック
            //200であればおｋ
            if (conn.getResponseCode() != 200) {
                System.out.println("エラー：APIに接続できませんでした。");
                return null;
            } else {
                //APIの情報を保存
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                //jsonデータをstringbuliderで読み込みと保存
                while (scanner.hasNext()) {
                    resultJson.append(scanner.nextLine());
                }

                //scannerを閉じる
                scanner.close();

                //urlを閉じる
                conn.disconnect();

                //Jsonオブジェクトから文字列Jsonを解析
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                //location nameから作られたAPIデータのリストを取得
                JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
                return locationData;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //locationを発見できず
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            //接続を試す
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();


            //リクエストを”GET”にする
            conn.setRequestMethod("GET");

            //APIに接続
            conn.connect();
            return conn;
        } catch (IOException e) {
            e.printStackTrace();
        }

        //接続できなかった時
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime();

        //時間リストを探ってどれが現在時刻と一致するか確認
        for (int i = 0; i < timeList.size(); i++) {
            String time = (String) timeList.get(i);
            if (time.equals(currentTime)) {
                //インデックスを返す
                return i;
            }
        }

        return 0;
    }

    public static String getCurrentTime() {

//      今の時間とデータを取得
        LocalDateTime currentDateTime = LocalDateTime.now();

//      データの形を2024-04-10T00:00にする
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

//      今のデータと時間を表示かつフォーマットする
        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;

    }

    // 天気予想コードを変換する
    private static String convertWeatherCode(long weatherCode) {
        String weatherCondition = "";
        if (weatherCode == 0L) {
//            晴れ
            weatherCondition = "Clear";
        } else if (weatherCode <= 3L && weatherCode >= 0L) {
//            曇り
            weatherCondition = "Cloudy";
        } else if ((weatherCode <= 67L && weatherCode >= 51L) || (weatherCode <= 99L && weatherCode >= 80L)) {
//            雨
            weatherCondition = "Rain";
        } else if ((weatherCode <= 77L && weatherCode >= 71L)) {
            //雪
            weatherCondition = "Snow";
        }

        return weatherCondition;
    }
}
