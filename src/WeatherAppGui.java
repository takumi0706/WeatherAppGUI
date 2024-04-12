import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.print.event.PrintJobAttributeListener;
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


        //GUIの立ち上げと題名を追加
        super("Weather APP");

        //アプリが閉じられたらGUIを終了するように設定
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //GUIのサイズ設定
        setSize(450, 650);

        //GUIを中央に表示
        setLocationRelativeTo(null);

        //レイアウト管理をなくして手動でGUI内の要素を配置できるように設定
        setLayout(null);

        //GUIのリサイズを防止
        setResizable(false);

        addGuiComponents();
    }

    private void addGuiComponents() {
        //検索欄の追加
        JTextField searchTextField = new JTextField();

        //検索欄の場所とサイズの設定
        searchTextField.setBounds(15, 15, 351, 45);

        //文字のフォントとサイズ変更
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));

        add(searchTextField);


        //天気の画像
        JLabel weatherConditionImage = new JLabel(loadImage("C:\\Users\\gannd\\IdeaProjects\\WeatherAppGUI\\src\\assets\\cloudy.png"));
        weatherConditionImage.setBounds(0, 125, 450, 217);
        add(weatherConditionImage);

        //温度(文字)の定義
        JLabel tempueratureText = new JLabel("10 ℃");
        tempueratureText.setBounds(0, 350, 450, 54);
        tempueratureText.setFont(new Font("Dialog", Font.BOLD, 48));

        //文字の中央に配置
        tempueratureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(tempueratureText);

        //天気状態を配置
        JLabel weatherConditionDesc = new JLabel("Cloudy");
        weatherConditionDesc.setBounds(0, 405, 450, 36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        //湿度のイメージ
        JLabel humidityImage = new JLabel(loadImage("C:\\Users\\gannd\\IdeaProjects\\WeatherAppGUI\\src\\assets\\humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        add(humidityImage);

        //湿度のテキスト
        JLabel humidityText = new JLabel("<html><b>Humidity<b> 100%<html>");
        humidityText.setBounds(90, 500, 85, 44);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        //風速のイメージ
        JLabel windspeedImage = new JLabel(loadImage("C:\\Users\\gannd\\IdeaProjects\\WeatherAppGUI\\src\\assets\\windspeed.png"));
        windspeedImage.setBounds(220, 500, 74, 66);
        add(windspeedImage);

//        風速のテキスト
        JLabel windspeedText = new JLabel("<html><b>Wind Speed<b> 10km/h<html>");
        windspeedText.setBounds(310, 500, 85, 55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windspeedText);

        //検索ボタンの追加
        JButton searchButton = new JButton(loadImage("C:\\Users\\gannd\\IdeaProjects\\WeatherAppGUI\\src\\assets\\search.png"));

        //カーソルを検索ボタンに置いたときにハンドカーソルに変更
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                ユーザーから位置を取得
                String userInput = searchTextField.getText();

//                入力に対して空白検知-空白排除
                if (userInput.replaceAll("\\s", "").length() <= 0) {
                    return;
                }

//            天気データを取得
                weatherData = WeatherApp.getWeatherData(userInput);

//                guiを更新

//                天気画像の更新
                String weatherCondition = (String) weatherData.get("weather_condition");

//                天気状態によって画像を変更
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

//                温度の更新
                double temperature = (double) weatherData.get("temperature");
                tempueratureText.setText(temperature + " ℃");

//                天気状態の更新
                weatherConditionDesc.setText(weatherCondition);

//                湿度の更新
                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Humidity<b> " + humidity + "%<html>");

//                風速の更新
                double windSpeed = (double) weatherData.get("windSpeed");
                windspeedText.setText("<html><b>Wind Speed<b> " + windSpeed + "km/h<html>");
            }
        });
        add(searchButton);
    }

    //GUI内の要素の画像生成用の関数
    private ImageIcon loadImage(String reasourcePath) {
        try {
            //指定したパスから画像を読み込み
            BufferedImage image = ImageIO.read(new File(reasourcePath));

            //反映できるようにイメージアイコンを返す
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("ソースが見つかりませんでした。");
        return null;
    }

}
