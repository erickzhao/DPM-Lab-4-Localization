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
	
	private static final float MAX_DISTANCE = 30;
	private static final float MOTOR_SPEED = 50;
	
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
			
			nav.setSpeeds(MOTOR_SPEED,-MOTOR_SPEED);
			
			while (true) {
				if (getFilteredData()>=MAX_DISTANCE) {
					nav.setSpeeds(0,0);
					break;
				}
			}
			// keep rotating until the robot sees a wall, then latch the angle
			
			nav.setSpeeds(MOTOR_SPEED,-MOTOR_SPEED);
			
			while (true) {
				if (getFilteredData()<MAX_DISTANCE) {
					nav.setSpeeds(0,0);
					break;
				}
			}
			
			angleA = odo.getAng();
			Sound.beep();
			
			// switch direction and wait until it sees no wall
			
			nav.setSpeeds(-MOTOR_SPEED,MOTOR_SPEED);
			
			while (true) {
				if (getFilteredData()>=MAX_DISTANCE) {
					nav.setSpeeds(0,0);
					break;
				}
			}
			
			// keep rotating until the robot sees a wall, then latch the angle
			
			nav.setSpeeds(-MOTOR_SPEED,MOTOR_SPEED);
			
			while (true) {
				if (getFilteredData()<MAX_DISTANCE) {
					nav.setSpeeds(0,0);
					break;
				}
			}
			
			angleB = odo.getAng();
			Sound.beep();
			
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'
			
			double endAngle = getEndAngle(angleA,angleB);
			if (endAngle<0) {
				nav.turnTo(endAngle+360,true);
			} else if (endAngle>360){
				nav.turnTo(endAngle-360,true);
			} else {
				nav.turnTo(endAngle, true);
			}
			
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
	
	public float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		float distance = usData[0]*100;
		
		if (distance > MAX_DISTANCE) distance = MAX_DISTANCE;
				
		return distance;
	}
	
	private double getEndAngle(double a, double b) {
//		if (a > b) {
//			return (odo.getAng()+225-(a+b)/2);
//		}
		return (odo.getAng()+45-(a+b)/2);
	}

}
