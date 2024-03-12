import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
// import org.openqa.selenium.remote.DesiredCapabilities
// import org.openqa.selenium.Proxy

driver = {
    // Not required
    // DesiredCapabilities capabilities = DesiredCapabilities.firefox() // Not preferred when use it without FirefoxOptions
    // Proxy proxy = new Proxy()
    // proxy.setProxyType(Proxy.ProxyType.DIRECT)
    // capabilities.setCapability("proxy", proxy)

    FirefoxOptions options = new FirefoxOptions()
    // options.merge(capabilities)
    options.addArguments('-headless')
    options.addArguments("--width=600")
    options.addArguments("--height=800")
    new FirefoxDriver(options)
}

reportsDir = "build/geb-reports"
