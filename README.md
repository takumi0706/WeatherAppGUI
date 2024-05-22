# WetherAppの詳細解説

## `AppLauncher`クラス

`AppLauncher`クラスは、Swingを使用してGUIを表示するためのエントリーポイントです。

### クラス定義
```java
import javax.swing.*;

public class AppLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Weather app GUIを表示
                new WeatherAppGui().setVisible(true);
            }
        });
    }
}
```

- `main`メソッド: Javaアプリケーションのエントリーポイントです。`SwingUtilities.invokeLater`を使用して、GUIの初期化をイベントディスパッチスレッドで実行します。これは、Swingのスレッドセーフティのために重要です。
- `new WeatherAppGui().setVisible(true);`: `WeatherAppGui`クラスのインスタンスを作成し、GUIを表示します。

## `WeatherApp`クラス

`WeatherApp`クラスは、外部APIから天気データを取得し、解析するバックエンドのロジックを含みます。

### クラス定義
```java
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class WeatherApp {
```

### `getWeatherData`メソッド
指定した場所の天気データを取得します。
```java
    public static JSONObject getWeatherData(String locationName) {
        JSONArray locationData = getLocationData(locationName);
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        String urlString = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude + "&longitude=" + longitude + "&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=Asia%2FTokyo";

        try {
            HttpURLConnection conn = fetchApiResponse(urlString);
            if (conn.getResponseCode() != 200) {
                System.out.println("エラー：APIに接続できませんでした。");
                return null;
            }

            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNext()) {
                resultJson.append(scanner.nextLine());
            }
            scanner.close();
            conn.disconnect();

            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            JSONArray weathercode = (JSONArray) hourly.get("weather_code");
            String weatherCondition = convertWeatherCode((long) weathercode.get(index));

            JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            JSONArray windspeedData = (JSONArray) hourly.get("wind_speed_10m");
            double windSpeed = (double) windspeedData.get(index);

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
```

- 指定された場所の天気データを取得し、JSONオブジェクトとして返します。
- まず、指定された場所の緯度と経度を取得します。
- 天気データを取得するためのURLを生成し、APIリクエストを送信します。
- 取得したデータを解析し、現在の時間のインデックスを見つけます。
- 温度、天気状態、湿度、風速を取得し、これらのデータを含むJSONオブジェクトを返します。

### `getLocationData`メソッド
指定した場所の緯度と経度を取得します。
```java
    public static JSONArray getLocationData(String locationName) {
        locationName = locationName.replaceAll(" ", "+");
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" + locationName + "&count=10&language=en&format=json";

        try {
            HttpURLConnection conn = fetchApiResponse(urlString);
            if (conn.getResponseCode() != 200) {
                System.out.println("エラー：APIに接続できませんでした。");
                return null;
            }

            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNext()) {
                resultJson.append(scanner.nextLine());
            }
            scanner.close();
            conn.disconnect();

            JSONParser parser = new JSONParser();
            JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));
            JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
            return locationData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
```

- 指定された場所の緯度と経度を取得し、JSONArrayとして返します。
- APIリクエストを送信し、結果を解析して緯度と経度のリストを取得します。

### `fetchApiResponse`メソッド
指定されたURLに対してAPIリクエストを送信します。
```java
    private static HttpURLConnection fetchApiResponse(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        return conn;
    }
```

- 指定されたURLに対してHTTP GETリクエストを送信し、HttpURLConnectionオブジェクトを返します。

### `findIndexOfCurrentTime`メソッド
現在の時間に対応するインデックスを見つけます。
```java
    private static int findIndexOfCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime();
        for (int i = 0; i < timeList.size(); i++) {
            String time = (String) timeList.get(i);
            if (time.equals(currentTime)) {
                return i;
            }
        }
        return 0;
    }
```

- 現在の時間に対応するインデックスをJSONArrayから見つけて返します。

### `getCurrentTime`メソッド
現在の時間を特定のフォーマットで取得します。
```java
    public static String getCurrentTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");
        return currentDateTime.format(formatter);
    }
```

- 現在の時間をISO 8601フォーマットで取得し、文字列として返します。

### `convertWeatherCode`メソッド
天気予報コードを人間が理解できる形式に変換します。
```java
    private static String convertWeatherCode(long weatherCode) {
        if (weatherCode == 0L) {
            return "Clear";
        } else if (weatherCode <= 3L) {
            return "Cloudy";
        } else if ((weatherCode >= 51L && weatherCode <= 67L) || (weatherCode >= 80L && weatherCode <= 99L)) {
            return "Rain";
        } else if (weatherCode >= 71L && weatherCode <= 77L) {
            return "Snow";
        }
        return "Unknown";
    }
}
```

- 天気予報コードを対応する天気状態に変換して返します。

## `WeatherAppGui`クラス

`WeatherAppGui`クラスは、天気データを表示するためのGUIを提供します。

### クラス定義
```java
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WeatherAppGui extends JFrame {
    private JSONObject weatherData;

    public WeatherAppGui() {
        super("Weather APP");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 650);
        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);
        addGuiComponents();
    }
```

- コンストラクタでGUIの基本設定を行います。
- `super("Weather APP");`: ウィンドウのタイトルを設定します。
- `setDefaultCloseOperation(EXIT_ON_CLOSE);`: ウィンドウを閉じたときにアプリケーションを終了します。
- `setSize(450

, 650);`: ウィンドウのサイズを設定します。
- `setLocationRelativeTo(null);`: ウィンドウを画面の中央に配置します。
- `setLayout(null);`: レイアウトマネージャを無効にし、手動でコンポーネントを配置します。
- `setResizable(false);`: ウィンドウのサイズ変更を無効にします。
- `addGuiComponents();`: GUIコンポーネントを追加します。

### `addGuiComponents`メソッド
GUIコンポーネントを追加します。
```java
    private void addGuiComponents() {
        JTextField searchTextField = new JTextField();
        searchTextField.setBounds(15, 15, 351, 45);
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));
        add(searchTextField);

        JLabel weatherConditionImage = new JLabel(loadImage("C:\\Users\\gannd\\IdeaProjects\\WeatherAppGUI\\src\\assets\\cloudy.png"));
        weatherConditionImage.setBounds(0, 125, 450, 217);
        add(weatherConditionImage);

        JLabel tempueratureText = new JLabel("10 ℃");
        tempueratureText.setBounds(0, 350, 450, 54);
        tempueratureText.setFont(new Font("Dialog", Font.BOLD, 48));
        tempueratureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(tempueratureText);

        JLabel weatherConditionDesc = new JLabel("Cloudy");
        weatherConditionDesc.setBounds(0, 405, 450, 36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        JLabel humidityImage = new JLabel(loadImage("C:\\Users\\gannd\\IdeaProjects\\WeatherAppGUI\\src\\assets\\humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        add(humidityImage);

        JLabel humidityText = new JLabel("<html><b>Humidity<b> 100%<html>");
        humidityText.setBounds(90, 500, 85, 44);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        JLabel windspeedImage = new JLabel(loadImage("C:\\Users\\gannd\\IdeaProjects\\WeatherAppGUI\\src\\assets\\windspeed.png"));
        windspeedImage.setBounds(220, 500, 74, 66);
        add(windspeedImage);

        JLabel windspeedText = new JLabel("<html><b>Wind Speed<b> 10km/h<html>");
        windspeedText.setBounds(310, 500, 85, 55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windspeedText);

        JButton searchButton = new JButton(loadImage("C:\\Users\\gannd\\IdeaProjects\\WeatherAppGUI\\src\\assets\\search.png"));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userInput = searchTextField.getText();
                if (userInput.replaceAll("\\s", "").length() <= 0) {
                    return;
                }
                weatherData = WeatherApp.getWeatherData(userInput);
                String weatherCondition = (String) weatherData.get("weather_condition");

                switch (weatherCondition) {
                    case "Clear":
                        weatherConditionImage.setIcon(loadImage("C:\\Users\\gannd\\IdeaProjects\\WeatherAppGUI\\src\\assets\\clear.png"));
                        break;
                    case "Cloudy":
                        weatherConditionImage.setIcon(loadImage("C:\\Users\\gannd\\IdeaProjects\\WeatherAppGUI\\src\\assets\\cloudy.png"));
                        break;
                    case "Rain":
                        weatherConditionImage.setIcon(loadImage("C:\\Users\\gannd\\IdeaProjects\\WeatherAppGUI\\src\\assets\\rain.png"));
                        break;
                    case "Snow":
                        weatherConditionImage.setIcon(loadImage("C:\\Users\\gannd\\IdeaProjects\\WeatherAppGUI\\src\\assets\\snow.png"));
                        break;
                }

                double temperature = (double) weatherData.get("temperature");
                tempueratureText.setText(temperature + " ℃");

                weatherConditionDesc.setText(weatherCondition);

                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Humidity<b> " + humidity + "%<html>");

                double windSpeed = (double) weatherData.get("windSpeed");
                windspeedText.setText("<html><b>Wind Speed<b> " + windSpeed + "km/h<html>");
            }
        });
        add(searchButton);
    }
```

- 検索欄、天気の画像、温度、天気状態、湿度、風速のラベルを追加します。
- 検索ボタンを追加し、クリック時のアクションを設定します。ユーザーが検索欄に入力した場所の天気データを取得し、GUIを更新します。

### `loadImage`メソッド
指定されたパスから画像を読み込みます。
```java
    private ImageIcon loadImage(String reasourcePath) {
        try {
            BufferedImage image = ImageIO.read(new File(reasourcePath));
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("ソースが見つかりませんでした。");
        return null;
    }
}
```

- 指定されたパスから画像を読み込み、`ImageIcon`として返します。画像の読み込みに失敗した場合、エラーメッセージを表示します。

---

このコード全体は、指定された場所の天気データを取得し、GUIに表示するためのアプリケーションを実装しています。`AppLauncher`クラスがGUIのエントリーポイントとなり、`WeatherApp`クラスが天気データを取得して解析し、`WeatherAppGui`クラスがデータを表示します。
