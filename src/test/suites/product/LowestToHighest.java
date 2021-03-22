package product;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import utils.CommonSteps;
import utils.Constants;
import utils.DriverFactory;
import utils.MarkTest;

public class LowestToHighest extends DriverFactory {
	
	
	@Test
	//@Description("LowestToHighest")
	public void main() {
		// TODO Auto-generated method stub

		WebElement order = driver.findElement(By.cssSelector(Constants.LowestToHighestLocatorOrderButton));

		order.click();

		WebElement LowesttoHighest = driver.findElement(By.cssSelector(Constants.LowestToHighestLocatorLowesttoHighest));

		LowesttoHighest.click();

		Constants.LowestToHighestProductCount = CommonSteps.productCount(driver);
		
		
				
		try {
			Thread.sleep(1000);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
		Constants.LowestToHighestLastCost = CommonSteps.findCostByIndex(driver, 0);
		
		int flag = 0;
		
		for (int i = 1; i < Constants.LowestToHighestProductCount; i++) {
			
            Constants.LowestToHighestcurrentCost = CommonSteps.findCostByIndex(driver, i);
            
            System.out.println("CurrentCost :" +Constants.LowestToHighestcurrentCost);
            
            System.out.println("LastCost :" +Constants.LowestToHighestLastCost);
		
            if(Constants.LowestToHighestcurrentCost < Constants.LowestToHighestLastCost) {
            	
            	flag = 1;
            	
            }
            
            Constants.LowestToHighestLastCost = Constants.LowestToHighestcurrentCost;
            
		}
		
			
		MarkTest.LowestToHighest(driver, flag);

		
		
	}
	 
	

}
