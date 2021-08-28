package org.firstinspires.ftc.teamcode;

import android.os.SystemClock;
import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp
public class CircularOdometry extends LinearOpMode {

    DcMotor fLeft;
    DcMotor fRight;
    DcMotor bLeft;
    DcMotor bRight;

    @Override
    public void runOpMode() throws InterruptedException {
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

        double mecanumOffset = 4.631; //inches
        //encoder values
        //left is fLeft
        //right is fRight
        //back is bLeft
        double leftEncoder = 0;
        double leftEncoderChange;
        double rightEncoder = 0;
        double rightEncoderChange;
        double backEncoder = 0;
        double backEncoderChange = 0;

        //encoder change when turning in positive angle direction
        //right up, left down

        //should start at 90 degrees since right is 0
        double angle = Math.PI/2;
        double angleChange = 0;
        double ticksPerTurn = 90000; //sum of encoders for 360 turn
        //position in encoder ticks
        double xPos = 0;
        double yPos = 0;
        double localX;
        double localY;
        double globalXChange;
        double globalYChange;

        while (opModeIsActive()) {
            //GAME-PAD CONTROLLING ROBOT
            //forward on the joystick is negative
            double power = -gamepad1.left_stick_y;
            double steer = gamepad1.right_stick_x;
            double strafe = 0;//gamepad1.left_stick_x;

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

            //ODOMETRY
            leftEncoderChange = fLeft.getCurrentPosition() - leftEncoder;
            rightEncoderChange = fRight.getCurrentPosition() - rightEncoder;
            backEncoderChange = bLeft.getCurrentPosition() - backEncoder;

            leftEncoder = fLeft.getCurrentPosition();
            rightEncoder = fRight.getCurrentPosition();
            backEncoder = bLeft.getCurrentPosition();

            double d = -(rightEncoderChange + leftEncoderChange)/2;
            //double a = backEncoderChange - Math.abs(toTicks(angleChange * mecanumOffset));

            angleChange = (-2.8 * Math.PI * (rightEncoderChange - leftEncoderChange))/ticksPerTurn;
                         //+ toInches(a)/mecanumOffset;
            angle += angleChange;
            if (angleChange == 0){
                angleChange = .00000001;
            }
            if (angleChange == 0){
                //odometry with simplifications
                localX = 0;//a;
                localY = d;

                double[] globalChange = toGlobal(localX, localY, angle);
                globalXChange = globalChange[0];
                globalYChange = globalChange[1];
            }else{
                //odometry with radius
                scale = -angleChange/Math.abs(angleChange);

                //with abs value the change is correct but all positive
                //without abs value the change is basically nothing
                double r =  d/angleChange;
                //double r_a = a/angleChange;

                localX = (scale*r*(1-Math.cos(angleChange)));// +
                        //(r_a*Math.sin(Math.abs(angleChange)));

                localY = (r*Math.sin(Math.abs(angleChange)));// +
                       // (-scale*r_a*(1-Math.cos(angleChange)));

                double[] globalChange = toGlobal(localX, localY, angle);
                globalXChange = globalChange[0];
                globalYChange = globalChange[1];
            }
            xPos += globalXChange;
            yPos += globalYChange;

            //TELEMETRY
            //the position is in inches
            telemetry.addData("right encoder", rightEncoder);
            telemetry.addData("left encoder", leftEncoder);
            telemetry.addData("back encoder", backEncoder);

            telemetry.addData("x position", toInches(xPos));//12*xPos/11550
            telemetry.addData("y position", toInches(yPos));//12*yPos/11550
            telemetry.addData("local x", localX);
            telemetry.addData("local y", localY);
            telemetry.addData("angleChange", 180*angleChange/Math.PI);
            telemetry.addData("angle", 180*angle/Math.PI);
            telemetry.update();

            //logcat stuff
            final String TAG = "MyActivity";
            if (SystemClock.elapsedRealtime() % 1 == 0){
                Log.i(TAG, "y position: " + globalYChange);
                Log.i(TAG, "y local: " + localY);
                Log.i(TAG, "angle change: " + angleChange);
                Log.i(TAG, "linear odometry being used: " + (angleChange == 0));
                Log.i(TAG, "  " + "  ");
                //DISCOVERIES
                //angle angle change is positive, local change is negative. same thing over way
                //no angle change is negative
            }
        }
    }

    public double numToDivide(double a, double b, double c, double d) {
        double output;
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
    public double[] toGlobal(double localX, double localY, double angle){
        double[] output = new double[2];

        output[0] = (Math.cos(angle) * localY) + (Math.sin(angle) * localX);
        output[1] = (Math.sin(angle)*localY) - (Math.cos(angle)*localX);

        return output;
    }
    public double toInches(double encoderTicks){
        return 12*encoderTicks/11500;
    }
    public double toTicks(double inches) {return inches*11500/12;}

}
