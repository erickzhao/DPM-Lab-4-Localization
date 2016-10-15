package ev3Localization;

import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };

	private Odometer odo;
	private SampleProvider usSensor;
	private float[] usData;
	private LocalizationType locType;
	private Navigation nav;
	
	public USLocalizer(Odometer odo,  SampleProvider usSensor, float[] usData, LocalizationType locType) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
		this.locType = locType;
		this.nav = new Navigation(odo);
	}
	
	public void doLocalization() {
		double [] pos = new double [3];
		double angleA, angleB;
		
		if (locType == LocalizationType.FALLING_EDGE) {
			
			// rotate the robot until it sees no wall
			
			nav.setSpeeds(75,-75);
			
			while (true) {
				if (getFilteredData()>=50) {
					nav.setSpeeds(0,0);
					break;
				}
			}
			// keep rotating until the robot sees a wall, then latch the angle
			
			nav.setSpeeds(75,-75);
			
			while (true) {
				if (getFilteredData()<50) {
					nav.setSpeeds(0,0);
					break;
				}
			}
			
			angleA = odo.getAng();
			Sound.beep();
			
			// switch direction and wait until it sees no wall
			
			nav.setSpeeds(-75,75);
			
			while (true) {
				if (getFilteredData()>=50) {
					nav.setSpeeds(0,0);
					break;
				}
			}
			
			// keep rotating until the robot sees a wall, then latch the angle
			
			nav.setSpeeds(-75,75);
			
			while (true) {
				if (getFilteredData()<50) {
					nav.setSpeeds(0,0);
					break;
				}
			}
			
			angleB = odo.getAng();
			Sound.beep();
			
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'
			
			nav.turnTo((angleA+angleB)/2-45,true);
			
			// update the odometer position (example to follow:)
			
			odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
		} else {
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */
			
			//
			// FILL THIS IN
			//
		}
	}
	
	private float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		float distance = usData[0]*100;
		
		if (distance > 50) distance = 50;
				
		return distance;
	}

}
