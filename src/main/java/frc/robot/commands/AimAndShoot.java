// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants;
import frc.robot.PolynomialFunction;
import frc.robot.subsystems.Accumulator;
import frc.robot.subsystems.Kicker;
import frc.robot.subsystems.Tower;
import frc.robot.subsystems.Shooter.Flywheel;
import frc.robot.subsystems.Shooter.Hood;
import frc.robot.subsystems.Shooter.Turret;
import frc.robot.subsystems.Shooter.TurretVision;

public class AimAndShoot extends CommandBase {
	private Flywheel flywheel;
	private Turret turret;
	private Hood hood;
	private TurretVision turretVision;
	private boolean finished;
	private boolean reverse;
	private double[] hoodRangingCoefficients = new double[] { 1, 2, 3 };
	private Accumulator accumulator;
	private Tower tower;
	private Kicker kicker;

	/** Creates a new AimAndShoot. */
	public AimAndShoot(Flywheel f, Turret t, Hood h, Accumulator a, Tower tw, Kicker k, TurretVision tv) {
		flywheel = f;
		turret = t;
		hood = h;
		accumulator = a;
		tower = tw;
		kicker = k;
		turretVision = tv;

		addRequirements(flywheel, turret, hood, accumulator, tower, kicker, turretVision);
		// Use addRequirements() here to declare subsystem dependencies.
	}

	// Called when the command is initially scheduled.
	@Override
	public void initialize() {
		flywheel.setSetpoint(Constants.FLYWHEEL_SHOOTING_SPEED);
	}

	// Called every time the scheduler runs while the command is scheduled.
	@Override
	public void execute() {
		if (turretVision.hasTargets()) {
			// turret.setSetpoint(turret.getMeasurement() - turretVision.xAngle());
			hood.setSetpoint(
					PolynomialFunction.polynomailFunction(turretVision.distanceFromTarget(), hoodRangingCoefficients));
			if (hood.atSetpoint() /* && turret.atSetpoint() */ && flywheel.atSetpoint()) {
				kicker.setMotors(Constants.KICKER_SPEED);
			}
			else {
				kicker.stopMotors();
			}
		}
		// else {
		// if (turret.atLeftLimit()) {
		// reverse = true;
		// }
		// else if (turret.atRightLimit()) {
		// reverse = false;
		// }
		// if (reverse) {
		// turret.setMotor(-(Constants.TURRET_SCAN_SPEED));
		// }
		// else {
		// turret.setMotor(Constants.TURRET_SCAN_SPEED);
		// }
		// }
		if (!kicker.hasBall()) {
			kicker.setMotors(Constants.KICKER_SPEED);
			tower.setMotors(Constants.TOWER_SPEED);
			accumulator.setMotors(Constants.ACCUMULATOR_SPEED);
		}
		else if (kicker.hasBall()) {
			accumulator.stopMotors();
			tower.stopMotors();
		}
		if (kicker.hasBall() && !turretVision.hasTargets()) {
			kicker.stopMotors();
		}

	}

	// Called once the command ends or is interrupted.
	@Override
	public void end(boolean interrupted) {
		flywheel.stop();
		// turret.stop();
		hood.stop();
		accumulator.stopMotors();
		tower.stopMotors();
		kicker.stopMotors();
	}

	// Returns true when the command should end.
	@Override
	public boolean isFinished() {
		return finished;
	}
}
