package org.firstinspires.ftc.teamcode;

import android.os.SystemClock;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp
public class PlebTest extends LinearOpMode {

    DcMotor fLeft;
    DcMotor fRight;
    DcMotor bLeft;
    DcMotor bRight;

    @Override
    public void runOpMode() throws InterruptedException {
        //encoder values
        //left is fLeft
        //right is fRight
        //back is bLeft

        double leftEncoder = 0;
        double leftEncoderChange = 0;
        double rightEncoder = 0;
        double rightEncoderChange = 0;
        double backEncoder = 0;
        double backEncoderChange = 0;

        //encoder change when turning in positive angle direction
        //right up, left down
        double angle = 0;
        double angleChange = 0;
        double ticksPerTurn = 90000; //sum of encoders for 360 turn
        //position in encoder ticks
        double xPos = 0;
        double yPos = 0;

        double currentYChange;
        //inches
        double mecanumOffset = 4.631;


        fLeft = hardwareMap.dcMotor.get("fLeft");
        fRight = hardwareMap.dcMotor.get("fRight");
        bLeft = hardwareMap.dcMotor.get("bLeft");
        bRight = hardwareMap.dcMotor.get("bRight");

        fLeft.setDirection(DcMotorSimple.Direction.FORWARD);
        fRight.setDirection(DcMotorSimple.Direction.REVERSE);
        bLeft.setDirection(DcMotorSimple.Direction.FORWARD);
        bRight.setDirection(DcMotorSimple.Direction.REVERSE);

        fLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        fRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        bLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        bRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        fLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        fRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        bLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        bRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        waitForStart();

        while (opModeIsActive()) {
            //GAME-PAD CONTROLLING ROBOT
            //forward on the joystick is negative
            double power = -gamepad1.left_stick_y;
            double steer = gamepad1.right_stick_x;
            double strafe = gamepad1.left_stick_x;

            double fLeftPower = power + steer + strafe;
            double fRightPower = power - steer - strafe;
            double bLeftPower = power + steer - strafe;
            double bRightPower = power - steer + strafe;

            double scale = numToDivide(fLeftPower,fRightPower,bLeftPower, bRightPower);

            //strafe pos is right
            fLeft.setPower(fLeftPower/scale);
            fRight.setPower(fRightPower/scale);
            bLeft.setPower(bLeftPower/scale);
            bRight.setPower(bRightPower/scale);

            //ODOMETRY LOCALIZATION
            leftEncoderChange = fLeft.getCurrentPosition() - leftEncoder;
            rightEncoderChange = fRight.getCurrentPosition() - rightEncoder;
            backEncoderChange = bLeft.getCurrentPosition() - backEncoder;
            leftEncoder = fLeft.getCurrentPosition();
            rightEncoder = fRight.getCurrentPosition();
            backEncoder = bLeft.getCurrentPosition();

            angleChange = 2.2 * Math.PI * (rightEncoderChange - leftEncoderChange)/ticksPerTurn;
            angle += angleChange;
            double xDis = -(leftEncoderChange + rightEncoderChange)/2;
            double yDis = backEncoderChange - (angleChange * mecanumOffset);
            xPos += (xDis * Math.sin(angle)) + (yDis * Math.cos(angle));
            yPos += (xDis * Math.cos(angle)) - (yDis * Math.sin(angle));

            //TELEMETRY
            telemetry.addData("Time", SystemClock.elapsedRealtime());

            telemetry.addData("gamepad1 right stick y", gamepad1.left_stick_y);

            telemetry.addData("fLeft encoder", fLeft.getCurrentPosition());
            telemetry.addData("fRight encoder", fRight.getCurrentPosition());
            telemetry.addData("bRight encoder", bRight.getCurrentPosition());

            //the position is in inches
            telemetry.addData("x position", 12*xPos/11550);
            telemetry.addData("y position", 12*yPos/11550);
            telemetry.addData("angle", 180*angle/Math.PI);
            telemetry.update();
        }
    }

    public double numToDivide(double a, double b, double c, double d) {
        double output = 0;
        double n1 = Math.abs(a);
        double n2 = Math.abs(b);
        double n3 = Math.abs(c);
        double n4 = Math.abs(d);

        //output will be positive
        output = Math.max(n1, Math.max(n2, Math.max(n3, n4)));
        if (output < 1){
            return 1;
        }else{
            return output;
        }
    }
}
