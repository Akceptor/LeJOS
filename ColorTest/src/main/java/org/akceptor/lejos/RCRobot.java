package org.akceptor.lejos;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.RegulatedMotor;

public class RCRobot {

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
		System.out.println("Press any key to start");
		Button.waitForAnyPress();
		System.out.println("Press ESCAPE key to stop");
		int keys = 0;
		while (keys != 32 /* ESCAPE */) {
			byte[] commands = { 0, 0, 0, 0 };
			// Get array of commands: one per channel
			((EV3IRSensor) sensor).getRemoteCommands(commands, 0, 4);
			// Current command and channel
			int command = maxValue(commands);
			int multiplier = maxIndex(commands);
			// Speed depends on channel
			// 200->400->600->800
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
				m2.startSynchronization();
				m1.forward();
				m2.forward();
				m2.endSynchronization();
				break;
			case 6:
				// rotate left
				m2.startSynchronization();
				m1.backward();
				m2.forward();
				m2.endSynchronization();
				break;
			case 7:
				// rotate right
				m2.startSynchronization();
				m1.forward();
				m2.backward();
				m2.endSynchronization();
				break;
			case 8:
				// both bwd
				m2.startSynchronization();
				m1.backward();
				m2.backward();
				m2.endSynchronization();
				break;
			default:
				// stop
				m2.startSynchronization();
				m1.stop();
				m2.stop();
				m2.endSynchronization();
			}
			// Acquire keys
			keys = Button.readButtons();
		}

	}

	/**
	 * Returns max. value for array
	 * 
	 * @param bytes
	 *            aray
	 * @return max value
	 */
	private static int maxValue(byte[] bytes) {
		int max = bytes[0];
		for (int ktr = 0; ktr < bytes.length; ktr++) {
			if (bytes[ktr] > max) {
				max = bytes[ktr];
			}
		}
		return max;
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
