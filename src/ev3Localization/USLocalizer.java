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
	
	private boolean noiseZone = false;
	private static final float MAX_DISTANCE = 50;
	private static final float EDGE_DISTANCE = 20;
	private static final float MARGIN_DISTANCE = 1;
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
		double angleA = 0;
		double angleB = 0;
		
		if (locType == LocalizationType.FALLING_EDGE) {
			
			// rotate the robot until it sees no wall
			
			nav.setSpeeds(MOTOR_SPEED,-MOTOR_SPEED);
			
			while (true) {
				if (getFilteredData()>=MAX_DISTANCE) {
					break;
				}
			}
			// keep rotating until the robot sees a wall, then latch the angle
			
		
			
			while (true) {
				if (!noiseZone && getFilteredData()<EDGE_DISTANCE+MARGIN_DISTANCE) {
					angleA = odo.getAng();
					noiseZone = true;
					Sound.beep();
				} else if ( getFilteredData()<EDGE_DISTANCE-MARGIN_DISTANCE){
					angleA = (angleA + odo.getAng())/2;
					noiseZone = false;
					Sound.beep();
					break;
				}
			}
			
			
			
			// switch direction and wait until it sees no wall
			
			nav.setSpeeds(-MOTOR_SPEED,MOTOR_SPEED);
			
			while (true) {
				if (getFilteredData()>=MAX_DISTANCE) {
					break;
				}
			}
			
			// keep rotating until the robot sees a wall, then latch the angle
			

			
			while (true) {
				if (!noiseZone && getFilteredData()<EDGE_DISTANCE+MARGIN_DISTANCE) {
					angleB = odo.getAng();
					noiseZone = true;
					Sound.beep();
				} else if ( getFilteredData()<EDGE_DISTANCE-MARGIN_DISTANCE){
					angleB = (angleB + odo.getAng())/2;
					noiseZone = false;
					Sound.beep();
					break;
				}
			}
			
			
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
		if (a > b) {
			return ((a+b)/2-225);
		}
		return ((a+b)/2-45);
	}

}
