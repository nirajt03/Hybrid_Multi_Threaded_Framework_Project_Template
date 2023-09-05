package commonUtility;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import excelUtilities.ExcelUtilities;
import exceptions.FileDoesNotExistsException;
import exceptions.InCorrectConfigConfigParameters;
import helperTestUtility.BrowserFactory;
import helperTestUtility.DriverFactory;
import helperTestUtility.RetryListerner;
import pageObjectModels.LoginPage;
import reportUtilities.MultiThreadedReportingUtility;
import runner.TestNGSuite;
import screenRecorderUtilities.ScreenRecorderUtil;
import screenRecorderUtilities.ScreenRecorderUtil.TypeOfScreen;

/**
 * Base Test
 * 
 * @author Niraj.Tiwari
 */
@Listeners({RetryListerner.class, MultiThreadedReportingUtility.class})
//@Listeners(MultiThreadedReportingUtility.class)
public class BaseTest {

	public static final Logger logger = LogManager.getLogger(BaseTest.class);

	protected BrowserFactory bf = new BrowserFactory();
	protected WebDriver driver;
	ChromeOptions options;

	String path= System.getProperty("testScriptName");
	public ArchUtilities archUtil= new ArchUtilities();

	String testScriptName;
	String className;
	private int passedTests=0;
	private int failedTests=0;
	private int skipedTests=0;

	public BaseTest() {

	}

	/**
	 * Get Class Name
	 * @return
	 */
	public String getClassName() {
		String packageName = this.getClass().getPackage().getName().trim();
		logger.info("Qualified Test Package name : " + packageName);
		return packageName;
	}

	@BeforeSuite(alwaysRun=true)
	public void beforeSuite()  {
		int thresholdDays = 10;
		String testClassName = getClassName();
		try {
			ScreenRecorderUtil.startRecord(TypeOfScreen.RegularScreen,testClassName);
			ScreenRecorderUtil.deleteOlderFilesAndDirectories(thresholdDays, TimeUnit.DAYS,".avi");			
			logger.info("Screen Recording Started ..!!");
		} catch (Exception e) {
			e.printStackTrace();	
		}
		//from test ng suite
		//TestNg class's part - must set below properties
		String  path = System.getProperty("user.dir") + "\\src\\test\\resources\\testdata\\hybridFrameworkTestDriver.xlsx";
		try {
			TestNGSuite.validateInputFile(path);
		} catch (FileDoesNotExistsException e) {
			e.printStackTrace();
		}
		System.setProperty("driverFilePath", path);
		try {
			System.setProperty("url",TestNGSuite.geturl());
		} catch (InCorrectConfigConfigParameters e) {
			e.printStackTrace();
		}
		System.setProperty("AppName Details","Pluralsight");
		System.setProperty("Environment Details","QA");
		String applicationTitle = "Pluralsight";
		System.setProperty("Application Title", applicationTitle +" Automation");
		System.setProperty("Report Name",applicationTitle+" Automation Test Report");
	}

	//		@BeforeClass(alwaysRun = true)
	//		public void beforeClass(ITestContext testcontext) {
	//			options = new ChromeOptions();
	//			options.addArguments("start-maximized");
	//			options.addArguments("--remote-allow-origins=*");	
	//			options.setBinary("116");
	//			//options.setBrowserVersion("116");
	//	
	//			//Map<String, Object> prefs = new HashMap<>();
	//			//prefs.put("profile.default_content_setting_values.media_stream_mic", 2); // enable/disable mic or camera permissions
	//			//value "1" is used for allowing the option, "2" -- for blocking.
	//			//prefs.put("profile.default_content_setting_values.media_stream_camera", 1);
	//			//options.setExperimentalOption("prefs", prefs);
	//	
	//			System.setProperty("webdriver.http.factory", "jdk-http-client");
	//			this.driver = new ChromeDriver(options);
	//			testcontext.setAttribute("driver", driver);
	//			logger.info("Chrome Browser Initiated successfully");
	//		}

	@BeforeClass(alwaysRun = true)
	public void beforeClass(ITestContext testcontext) {
		DriverFactory.getInstance().setDriver(bf.createBrowserInstance("Chrome"));
		driver = DriverFactory.getInstance().getDriver();
		testcontext.setAttribute("driver", driver);
		logger.info("Chrome Browser Initiated successfully");
	}

	@AfterMethod(alwaysRun=true)
	public void afterMethod(ITestResult result) {
		testScriptName = result.getMethod().getMethodName();
		System.setProperty("testScriptName", testScriptName);
		try {
			if (result.getStatus() == ITestResult.SUCCESS) {
				++passedTests;
			} else if (result.getStatus() == ITestResult.FAILURE) {
				++failedTests;
			} else if (result.getStatus() == ITestResult.SKIP) {
				++skipedTests;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterClass(alwaysRun = true)
	public void afterClass() {
		try {
			int totalTestCases = ExcelUtilities.getTotalTestCases(passedTests,failedTests,skipedTests);
			double passPercentage = ExcelUtilities.calculatePercentage(passedTests,totalTestCases);
			logger.info("Pass Percentage : "+passPercentage+" %");
		} catch (Exception e) {
			System.out.println(e.getCause());
		}finally {
			//driver.close();//driver.quit();
			DriverFactory.getInstance().closeDriver();
			logger.info("Chrome Browser Closed");
		}
	}

	@AfterSuite(alwaysRun=true)
	public void afterSuite() {
		try {
			ScreenRecorderUtil.stopRecord();
			logger.info("Screen Recording Stopped ..!!");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
		}
	}

	/**
	 * Open Application : URL
	 * @param url
	 * @return
	 */
	protected LoginPage openApplication(String url) {
		this.driver.get(url);
		logger.info("Login Page Displayed");
		return (new LoginPage(driver));
	}

	/** 
	 * This method is used to covert String value to boolean
	 * @param value
	 * @return
	 */
	public boolean stringToBoolean(String value) {
		boolean flag = false;
		value = value.toUpperCase();
		if (value.equalsIgnoreCase("Y") || value.equalsIgnoreCase("YES")) {
			flag = true;
		} else if (value.equals("N") || value.equalsIgnoreCase("NO")) {
			flag = false;
		}
		return flag;
	}	

}
