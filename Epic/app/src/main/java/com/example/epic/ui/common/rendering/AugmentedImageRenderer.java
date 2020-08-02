/*
 * Copyright 2018 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.epic.ui.common.rendering;

import android.content.Context;
import android.opengl.Matrix;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Pose;

import java.io.IOException;

/**
 * The Majority of this code was written by Google and has been reused/edited to fit this project
 * If you wish to view the pre-edited code it can be found here: https://github.com/google-ar/arcore-android-sdk/tree/master/samples/augmented_image_java
 * Also, where comments do not exist, I have commented on them to exhibit my understanding of the work.
 */

/** Renders an augmented image. */
public class AugmentedImageRenderer {

  private static final float TINT_INTENSITY = 0.1f;
  private static final float TINT_ALPHA = 1.0f;
  private static final int[] TINT_COLORS_HEX = {
          0x000000, 0xF44336, 0xE91E63, 0x9C27B0, 0x673AB7, 0x3F51B5, 0x2196F3, 0x03A9F4, 0x00BCD4,
          0x009688, 0x4CAF50, 0x8BC34A, 0xCDDC39, 0xFFEB3B, 0xFFC107, 0xFF9800,
  };

  private double wQuart;
  private double xQuart;
  private double yQuart;
  private double zQuart;
  private double xAngle;

  /*
  Too Objects being rendered to display default test 3D model
  This could be replaced with many different objects, animals, videos anything is possible.
   */

  private final ObjectRenderer imageFrame = new ObjectRenderer();
  private final ObjectRenderer virtualObjectShadow = new ObjectRenderer();

  public AugmentedImageRenderer() {}

  /*
  Initialise Rendering Objects, Using Base Andy model for testing purposes of this application
  Models could be replaced with anything.
   */
  public void createOnGlThread(Context context) throws IOException {

    imageFrame.createOnGlThread(
            context, "models/andy.obj", "models/andy.png");
    imageFrame.setMaterialProperties(0.0f, 3.5f, 1.0f, 6.0f);
    imageFrame.setBlendMode(ObjectRenderer.BlendMode.SourceAlpha);

    virtualObjectShadow.createOnGlThread(
            /*context=*/
    context, "models/andy_shadow.obj", "models/andy_shadow.png");
    virtualObjectShadow.setBlendMode(ObjectRenderer.BlendMode.Shadow);
    virtualObjectShadow.setMaterialProperties(1.0f, 0.0f, 0.0f, 1.0f);


  }

  /**
   * This method is responsible for displaying the 3D Model
   * Some changes to the rotations had be made to fix the display.
   */
  public void draw(
          float[] viewMatrix,
          float[] projectionMatrix,
          AugmentedImage augmentedImage,
          Anchor centerAnchor,
          float[] colorCorrectionRgba) {
    float[] tintColor = convertHexToColor(TINT_COLORS_HEX[augmentedImage.getIndex() % TINT_COLORS_HEX.length]);

    Pose anchorPose = centerAnchor.getPose();
    float scaleFactor = 1.0f;
    float[] modelMatrix = new float[16];
    anchorPose.toMatrix(modelMatrix, 0);

    /*
    https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles
    Code was rewritten in java and only reworked for the Roll(X-axis).
    Documented in dissertation document explaining why I'm removing the Roll Rotation from the model's matrix.
     */

    /*
    Get the Quaternions from the anchor for the recognised image.
     */
     wQuart = centerAnchor.getPose().qw();
     xQuart = centerAnchor.getPose().qx();
     yQuart = centerAnchor.getPose().qy();
     zQuart = centerAnchor.getPose().qz();

    // roll (x-axis rotation)
    double sinr_cosp = 2 * (wQuart * xQuart + yQuart * zQuart);
    double cosr_cosp = 1 - 2 * (xQuart * xQuart + yQuart * zQuart);
    xAngle = Math.atan2(sinr_cosp, cosr_cosp);

    // Convert Angles to degrees for matrix function.
    xAngle = Math.toDegrees(xAngle);

    /**
     * public static void rotateM (
     *                 float[] m,  source matrix
     *                 int mOffset,  index into m where the matrix starts
     *                 float a, angle to rotate in degrees
     *                 float x, X axis component
     *                 float y, Y axis component
     *                 float z, Z axis component )
     */
    // Remove Roll Rotation, Inverse xangle keeps the model upright, Screenshots in documentation explain what this function does.
    Matrix.rotateM(modelMatrix, 0, (float) -xAngle, 1f, 0f, 0f);

    /*
    Update the model and draw it.
     */
    imageFrame.updateModelMatrix(modelMatrix, scaleFactor);
    imageFrame.draw(viewMatrix, projectionMatrix, colorCorrectionRgba, tintColor);
    virtualObjectShadow.updateModelMatrix(modelMatrix, scaleFactor);
    virtualObjectShadow.draw(viewMatrix, projectionMatrix, colorCorrectionRgba, tintColor);

  }

  private static float[] convertHexToColor(int colorHex) {
    // colorHex is in 0xRRGGBB format
    float red = ((colorHex & 0xFF0000) >> 16) / 255.0f * TINT_INTENSITY;
    float green = ((colorHex & 0x00FF00) >> 8) / 255.0f * TINT_INTENSITY;
    float blue = (colorHex & 0x0000FF) / 255.0f * TINT_INTENSITY;
    return new float[] {red, green, blue, TINT_ALPHA};
  }
}
