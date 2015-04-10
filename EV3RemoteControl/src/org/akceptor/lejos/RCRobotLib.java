package org.akceptor.lejos;

import java.util.Arrays;
import java.util.Collections;

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

import org.apache.commons.lang3.ArrayUtils;

public class RCRobotLib {

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
			byte[] commands = { 0, 0, 0, 0, 0 };
			// Get array of commands: one per channel
			((EV3IRSensor) sensor).getRemoteCommands(commands, 0, 4);
			provider.fetchSample(sample, 0);
			if (sample[0] < 20) {
				avoidObstacleDetected(m1, m2);
			}
			// Current command and channel
			// Channel 0 is used for distance measurement now so can't be used
			// for RC
			int command = maxValue(Arrays.copyOfRange(commands, 1, 3));
			int multiplier = maxIndex(Arrays.copyOfRange(commands, 1, 3));
			// Speed depends on channel
			// NONE->400->600->800
			m1.setSpeed(200 * (1 + multiplier));
			m2.setSpeed(200 * (1 + multiplier));
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
			default:
				// stop
				goStop(m1, m2);
			}
			// Acquire keys
			keys = Button.readButtons();
		}

	}

	private static void avoidObstacleDetected(RegulatedMotor m1, RegulatedMotor m2) {
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
		return Collections.max(Arrays.asList(ArrayUtils.toObject(bytes)));
	}

	/**
	 * Returns index of max. element for array
	 * 
	 * @param bytes
	 *            array
	 * @return max element index
	 */
	private static int maxIndex(byte[] bytes) {
		int max = bytes[0];
		int maxIdx = 0;
		for (int ktr = 0; ktr < bytes.length; ktr++) {
			if (bytes[ktr] > max) {
				max = bytes[ktr];
				maxIdx = ktr;
			}
		}
		return maxIdx;
	}
}