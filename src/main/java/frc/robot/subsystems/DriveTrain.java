// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim;
import edu.wpi.first.wpilibj.simulation.EncoderSim;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class DriveTrain extends SubsystemBase {
	private CANSparkMax leftMotor1 = driveMotor(1, false);
	private CANSparkMax leftMotor2 = driveMotor(2, false);
	private CANSparkMax leftMotor3 = driveMotor(3, false);
	private CANSparkMax rightMotor1 = driveMotor(4, true);
	private CANSparkMax rightMotor2 = driveMotor(5, true);
	private CANSparkMax rightMotor3 = driveMotor(6, true);

	// TODO evaluate connecting to spark maxes, make sparkmax sim work
	private Encoder leftEncoder = new Encoder(0, 1);
	private Encoder rightEncoder = new Encoder(2, 3, true);

	private MotorControllerGroup leftMotors = new MotorControllerGroup(leftMotor1, leftMotor2, leftMotor3);
	private MotorControllerGroup rightMotors = new MotorControllerGroup(rightMotor1, rightMotor2, rightMotor3);

	private DifferentialDrive drive = new DifferentialDrive(leftMotors, rightMotors);

	// Create the simulation model of our drivetrain.
	// https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/drivesim-tutorial/drivetrain-model.html
	private DifferentialDrivetrainSim driveSim = new DifferentialDrivetrainSim(
			// fill in with characterization values once the chassis is done
			// LinearSystemId.identifyDrivetrainSystem(KvLinear, KaLinear, KvAngular, KaAngular),
			DCMotor.getNEO(3), // 2 NEO motors on each side of the drivetrain.
			7.29, // 7.29:1 gearing reduction.
			7.5, // MOI of 7.5 kg m^2 (from CAD model).
			60.0, // The mass of the robot is 60 kg.
			Units.inchesToMeters(4), // The robot uses 3" radius wheels.
			0.7112, // The track width is 0.7112 meters.

			// The standard deviations for measurement noise:
			// x and y: 0.001 m
			// heading: 0.001 rad
			// l and r velocity: 0.1 m/s
			// l and r position: 0.005 m
			VecBuilder.fill(0.001, 0.001, 0.001, 0.1, 0.1, 0.005, 0.005));
	private EncoderSim leftEncoderSim = new EncoderSim(leftEncoder);
	private EncoderSim rightEncoderSim = new EncoderSim(rightEncoder);
	private final Field2d field = new Field2d();

	private PIDController leftPIDController = new PIDController(Constants.LEFT_DRIVE_PID_CONSTANTS[0],
			Constants.LEFT_DRIVE_PID_CONSTANTS[1], Constants.LEFT_DRIVE_PID_CONSTANTS[2]);
	private PIDController rightPIDController = new PIDController(Constants.RIGHT_DRIVE_PID_CONSTANTS[0],
			Constants.RIGHT_DRIVE_PID_CONSTANTS[1], Constants.RIGHT_DRIVE_PID_CONSTANTS[2]);
	private boolean stop = true;

	public CANSparkMax driveMotor(int motorID, boolean inverted) {
		CANSparkMax sparkMax = new CANSparkMax(motorID, MotorType.kBrushless);
		sparkMax.restoreFactoryDefaults();
		sparkMax.setInverted(inverted);
		sparkMax.setIdleMode(IdleMode.kBrake);
		sparkMax.setSmartCurrentLimit(60);
		sparkMax.getEncoder().setPosition(0);
		sparkMax.burnFlash();
		return sparkMax;
	}

	public DriveTrain() {
		// TODO set distance per pulse and distance per rev
		// leftEncoder.setDistancePerPulse(distancePerRev/pulsesPerRev);
		// rightEncoder.setDistancePerPulse(distancePerRev/pulsesPerRev);
	}

	public void driveWithJoysticks(double xSpeed, double zRotation) {
		pidArcadeDrive(xSpeed, zRotation, true);
	}

	@Override
	public void periodic() {
		// This method will be called once per scheduler run
		SmartDashboard.putNumber("Left Encoder", getLeftPosition());
		SmartDashboard.putNumber("Right Encoder", getRightPosition());
		SmartDashboard.putNumber("Left Encoder Raw", leftEncoder.get());
		SmartDashboard.putNumber("Right Encoder Raw", rightEncoder.get());
		SmartDashboard.putNumber("Left Neo Encoder",
				leftMotor1.getEncoder().getPosition() / Constants.DRIVE_GEAR_RATIO);
		SmartDashboard.putNumber("Right Neo Encoder",
				rightMotor1.getEncoder().getPosition() / Constants.DRIVE_GEAR_RATIO);
		SmartDashboard.putNumber("Left Neo Encoder Raw", leftMotor1.getEncoder().getPosition());
		SmartDashboard.putNumber("Right Neo Encoder Raw", rightMotor1.getEncoder().getPosition());

		SmartDashboard.putData(leftPIDController);
		SmartDashboard.putData(rightPIDController);

		if (!stop) {
			drive.tankDrive(leftPIDOutput(), rightPIDOutput());
		}

	}

	@Override
	public void simulationPeriodic() {
		// This method will be called once per scheduler run during simulation
		driveSim.setInputs(leftMotor1.get() * RobotController.getBatteryVoltage(),
				rightMotor1.get() * RobotController.getBatteryVoltage());
		driveSim.update(0.02);
		leftEncoderSim.setDistance(driveSim.getLeftPositionMeters());
		leftEncoderSim.setRate(driveSim.getLeftVelocityMetersPerSecond());
		rightEncoderSim.setDistance(driveSim.getRightPositionMeters());
		rightEncoderSim.setDistance(driveSim.getRightVelocityMetersPerSecond());
		field.setRobotPose(driveSim.getPose());

	}

	public void stop() {
		drive.stopMotor();
		stop = true;
	}

	public double getLeftPosition() {
		return leftEncoder.get() * Constants.ENCODER_CONVERSION_FACTOR;
	}

	public double getRightPosition() {
		return rightEncoder.get() * Constants.ENCODER_CONVERSION_FACTOR;
	}

	public double getLeftRate() {
		return leftEncoder.getRate() * Constants.ENCODER_CONVERSION_FACTOR;
	}

	public double getRightRate() {
		return rightEncoder.getRate() * Constants.ENCODER_CONVERSION_FACTOR;
	}

	public void pidTankDrive(double leftSpeed, double rightSpeed) {
		leftPIDController.setSetpoint(leftSpeed);
		rightPIDController.setSetpoint(rightSpeed);
		stop = false;
	}

	public void pidArcadeDrive(double xSpeed, double zRotation, boolean squareInputs) {
		var speeds = DifferentialDrive.arcadeDriveIK(xSpeed, zRotation, squareInputs);
		pidTankDrive(speeds.left, speeds.right);
	}

	public double leftPIDOutput() {
		return leftPIDController.calculate(leftEncoder.get());
	}

	public double rightPIDOutput() {
		return rightPIDController.calculate(rightEncoder.get());
	}
}
