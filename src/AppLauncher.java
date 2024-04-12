import javax.swing.*;
import java.nio.file.Watchable;

public class AppLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //weather app　guiを表示
                new WeatherAppGui().setVisible(true);

//                 System.out.println(WeatherApp.getLocationData("Tokyo"));
//                System.out.println(WeatherApp.getCurrentTime());
//                System.out.println(WeatherApp.getWeatherData("Tokyo"));

            }
        });
    }
}
