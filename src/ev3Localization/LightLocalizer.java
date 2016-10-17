package ev3Localization;

import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

public class LightLocalizer {
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData;	
	private Navigation nav;
	
	private static final float MOTOR_SPEED = 75;
	private static final double ARM_LENGTH = 6.3;
	private static final double CENTER_TO_SENSOR = 10.2;
	private static final long CORRECTION_PERIOD = 255;
	private static final float BLACK = (float) 0.26;
	
	public LightLocalizer(Odometer odo, SampleProvider colorSensor, float[] colorData) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.nav = new Navigation(odo);
	}
	
	public void doLocalization() {
		// drive to location listed in tutorial
		// start rotating and clock all 4 gridlines
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees
		
		// The robot drive forward til desired x position
		nav.setSpeeds(MOTOR_SPEED,MOTOR_SPEED);
		double archivedX, archivedY;
		while (true){
			colorSensor.fetchSample(colorData, 0);
			float darkness = colorData[0];
			if (darkness < BLACK){//Detect the dark lines
				archivedX = odo.getX();
				Sound.beep();
				break;
			}
		}
		// extend the sensor arm before stopping the motors
		while (true){
			if (odo.getX()>=archivedX+ARM_LENGTH){
				nav.setSpeeds(0, 0);
				break;
			}
		}
		
		
		// Turn the robot 90 degrees towards y-axis, then move to appropriate position
		nav.turnTo(90, true);
		nav.setSpeeds(MOTOR_SPEED,MOTOR_SPEED);
		while (true){
			colorSensor.fetchSample(colorData, 0);
			float darkness = colorData[0];
			if (darkness < BLACK){ //Detect the dark lines
				archivedY = odo.getY();
				Sound.beep();
				break;
			}
		}
		// Similar to x-axis operation
		while (true){
			if (odo.getY()>=archivedY+ARM_LENGTH){
				nav.setSpeeds(0, 0);
				break;
			}
		}
		
		
		// Now determine the precise position
		// first, the y value
		
		double angleA = 0;
		double angleB = 0;
		double [] pos = new double [3];
		int PC = 0;
		

		
		nav.setSpeeds(MOTOR_SPEED, -MOTOR_SPEED);
		while (true){
			colorSensor.fetchSample(colorData, 0);
			float darkness = colorData[0];
			if (darkness < BLACK){ //Detect the dark lines
				if (PC ==0){
					angleA = odo.getAng();
					PC++;
					Sound.beep();
				} else if (PC ==1){
					PC++;
				} else {
					angleB = odo.getAng();
					PC = 0;
					Sound.beep();
					break;
				}
				// Make sure the robot won't detect the same line twice
				try {
					Thread.sleep(CORRECTION_PERIOD);
				} catch (InterruptedException e) {}
			}
		}

				
		pos[0] = -CENTER_TO_SENSOR*Math.cos(Math.toRadians(Math.abs((angleA-angleB)/2)));
			
		// Now the x-axis	
		while (true){
			colorSensor.fetchSample(colorData, 0);
			float darkness = colorData[0];
			if (darkness < BLACK){ //Detect the dark lines
				if (PC ==0){
					angleA = odo.getAng();
					PC++;
					Sound.beep();
				} else if (PC ==1){
					PC++;
				} else {
					angleB = odo.getAng();
					PC = 0;
					Sound.beep();
					break;
				}
				// Make sure the robot won't detect the same line twice
				try {
					Thread.sleep(CORRECTION_PERIOD);
				} catch (InterruptedException e) {}
			}
		}

		
		
		pos[1] = -CENTER_TO_SENSOR*Math.cos(Math.toRadians(Math.abs((angleA-angleB)/2)));
		nav.setSpeeds(0, 0);
		try {
			Thread.sleep(CORRECTION_PERIOD*4);
		} catch (InterruptedException e) {}
		// Now we need to correct the heading
		double angleCorrection;
		angleCorrection = 90 - Math.abs((angleA-angleB)/2) - angleB;
		pos[2] = odo.getAng() + angleCorrection;
		
		// Now set the accurate position to the odometer
		odo.setPosition(pos, new boolean [] {true, true, true});
		nav.travelTo(0, 0);
		nav.turnTo(0, true);
		
		
	}
	
	public double wrapAngle (double angle){
		if (angle<0) {
			return angle+360;
		} else if (angle>360){
			return angle-360;
		} else {
			return angle;
		}
	}

}
