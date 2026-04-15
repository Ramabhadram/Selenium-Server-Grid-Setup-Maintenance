import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.Duration;

/***********Notes **************************/
/* To start a new seession using Postman:
POST Method : http://localhost:4444/session
Headers: application/json; charset=utf-8
Body: {
  "capabilities": {
    "alwaysMatch": {
      "browserName": "chrome"
    }
  }
}
*[BrowserName - MicrosoftEdge for edge].
*/

/********************************************/

public class SeleniumServerLauncher {
    private static Process seleniumProcess;
    public static void startServer() throws Exception {
        seleniumProcess = new ProcessBuilder(
                "java",
                "-Dwebdriver.edge.driver=C:\\Development\\jars\\msedgedriver.exe",
                "-jar",
                "C:\\Development\\jars\\selenium-server-4.41.0.jar",
                "standalone",
                "--port", "4444",
                "--selenium-manager", "true"
        ).redirectErrorStream(true).start();

        // Wait until server is ready
        long end = System.currentTimeMillis() + Duration.ofSeconds(30).toMillis();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(seleniumProcess.getInputStream()))) {
            String line;
            while (System.currentTimeMillis() < end) {
                while (reader.ready() && (line = reader.readLine()) != null) {
                    System.out.println(line);
                    if (line.contains("Started Selenium Standalone")) {
                        return;
                    }
                }
                Thread.sleep(500);
            }
        }
        throw new RuntimeException("Selenium Server did not start within timeout");
    }
    public static void WindowsKillServer(){
       // Use this method as last resort if the server is not shutting down.
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "taskkill /F /IM java.exe");
            Process process = pb.start();
            // Read output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = process.waitFor();
            System.out.println("Exited with code: " + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void stopServer() {
        if (seleniumProcess != null) {
            seleniumProcess.destroy(); // graceful
            seleniumProcess.destroyForcibly(); // force kill if needed
            System.out.println("Selenium Server Grid destroy()");
        }
    }

    public static void runSeleniumTestBrowser() {
        WebDriver driver = null;
        try {
//       WindowsKillServer(); // Run this when the server can't be shutdown gracefully.
            startServer();
            driver = new RemoteWebDriver(URI.create("http://localhost:4444").toURL(), new EdgeOptions());
            driver.get("http://localhost:4444");
            System.out.println(driver.getTitle());
            Thread.sleep(600000);
        } catch(Exception ex){
            ex.printStackTrace();
        }
        finally {
            driver.quit();
            stopServer();
        }
    }
    public static void runSeleniumServerGridOnly() {
        try {
//       WindowsKillServer(); // Run this when the server can't be shutdown gracefully.
            startServer();
            Thread.sleep(600000); // Run the server grid for 10 mins and then shutdown;
        } catch(Exception ex){
            ex.printStackTrace();
        }
        finally {
            stopServer();
        }
    }

    public static void main(String[] args) {
       // runSeleniumTestBrowser();
       // WindowsKillServer();
    }
}