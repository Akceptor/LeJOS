package org.akceptor.lejos;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class RCRobotLib {

	private static boolean verbose = false;

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		// Motors
		RegulatedMotor m1 = new EV3LargeRegulatedMotor(MotorPort.A);
		RegulatedMotor m2 = new EV3LargeRegulatedMotor(MotorPort.B);
		m1.setSpeed(200);
		m2.setSpeed(200);
		m2.synchronizeWith(new RegulatedMotor[] { m1 });
		m2.startSynchronization();
		// IR Remote
		Port port = LocalEV3.get().getPort("S1");
		SensorModes sensor = new EV3IRSensor(port);
		SampleProvider provider = sensor.getMode("Distance");
		float[] sample = new float[provider.sampleSize()];

		System.out.println("Press any key to start");
		Button.waitForAnyPress();
		System.out.println("Press ESCAPE key to stop");
		int keys = 0;
		while (keys != 32 /* ESCAPE */) {
			// Control
			byte[] commands = { 0, 0, 0, 0 };
			// Get array of commands: one per channel
			((EV3IRSensor) sensor).getRemoteCommands(commands, 0, 4);
			// Current command and channel
			int command = maxValue(commands);
			int multiplier = maxIndex(commands);
			// Speed depends on channel
			// NONE->400->600->800
			m1.setSpeed(200 * (multiplier));
			m2.setSpeed(200 * (multiplier));
			if (verbose) {
				// logging
				System.out.println("Multiplier: " + multiplier);
				System.out.println("Keys: "
						+ commands[Math.abs(multiplier - 1)]);
			}
			switch (command) {
			case 1:
				// Left fwd
				m2.forward();
				break;
			case 2:
				// left bwd
				m2.backward();
				break;
			case 3:
				// right fwd
				m1.forward();
				break;
			case 4:
				// right bwd
				m1.backward();
				break;
			case 5:
				// both fwd
				goForward(m1, m2);
				break;
			case 6:
				// rotate left
				goLeft(m1, m2);
				break;
			case 7:
				// rotate right
				goRight(m1, m2);
				break;
			case 8:
				// both bwd
				goBackWard(m1, m2);
				break;
			case 10:
				// stop
				goStop(m1, m2);
				break;
			case 11:
				// logging
				verbose = !verbose;
				System.out.println("Verbose: " + verbose);
				break;
			default:
				provider.fetchSample(sample, 0);
				if (sample[0] < 20) {
					// avoid obstacle
					avoidObstacleDetected(m1, m2);
				} else {
					// stop
					goStop(m1, m2);
				}
			}
			// Acquire keys
			keys = Button.readButtons();
		}

	}

	private static void avoidObstacleDetected(RegulatedMotor m1,
			RegulatedMotor m2) {
		m1.setSpeed(500);
		m2.setSpeed(500);
		goBackWard(m1, m2);
		Sound.playTone(1000, 1000);
		Delay.msDelay(2000);
		goStop(m1, m2);
	}

	private static void goForward(RegulatedMotor m1, RegulatedMotor m2) {
		m2.startSynchronization();
		m1.forward();
		m2.forward();
		m2.endSynchronization();
	}

	private static void goLeft(RegulatedMotor m1, RegulatedMotor m2) {
		m2.startSynchronization();
		m1.backward();
		m2.forward();
		m2.endSynchronization();
	}

	private static void goRight(RegulatedMotor m1, RegulatedMotor m2) {
		m2.startSynchronization();
		m1.forward();
		m2.backward();
		m2.endSynchronization();
	}

	private static void goStop(RegulatedMotor m1, RegulatedMotor m2) {
		m2.startSynchronization();
		m1.stop();
		m2.stop();
		m2.endSynchronization();
	}

	private static void goBackWard(RegulatedMotor m1, RegulatedMotor m2) {
		m2.startSynchronization();
		m1.backward();
		m2.backward();
		m2.endSynchronization();
	}

	/**
	 * Returns max. value for array
	 * 
	 * @param bytes
	 *            array
	 * @return max value
	 */
	private static int maxValue(byte[] bytes) {
		if (bytes[3] > 0)
			return bytes[3];
		if (bytes[2] > 0)
			return bytes[2];
		if (bytes[1] > 0)
			return bytes[1];
		if (bytes[0] > 0)
			return bytes[0];
		return 0;
	}

	/**
	 * Returns index of max. element for array
	 * 
	 * @param bytes
	 *            array
	 * @return max element index
	 */
	private static int maxIndex(byte[] bytes) {
		if (bytes[3] > 0)
			return 4;
		if (bytes[2] > 0)
			return 3;
		if (bytes[1] > 0)
			return 2;
		if (bytes[0] > 0)
			return 1;
		return 0;
	}
}