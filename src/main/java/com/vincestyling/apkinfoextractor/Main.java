/*
 * Copyright 2014 Vince Styling
 * https://github.com/vince-styling/ApkInfoExtractor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vincestyling.apkinfoextractor;

import com.vincestyling.apkinfoextractor.core.ctrls.OnResizeListener;
import com.vincestyling.apkinfoextractor.entity.Solution;
import com.vincestyling.apkinfoextractor.core.ctrls.LaunchController;
import com.vincestyling.apkinfoextractor.core.ctrls.SplashController;
import com.vincestyling.apkinfoextractor.utils.Constancts;
import com.vincestyling.apkinfoextractor.core.ctrls.WizardController;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.InputStream;

public class Main extends Application {
	private Stage stage;

	public static void main(String[] args) {
		Application.launch(Main.class, (java.lang.String[]) null);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream(Constancts.APP_ICON)));
		primaryStage.setTitle(Constancts.APP_NAME);
		stage = primaryStage;
		showSplash();
		stage.show();
	}

	public void showSplash() throws Exception {
		SplashController splash = (SplashController) replaceSceneContent(Constancts.SPLASH_PAGE, 640, 400);
		splash.setApp(this);
	}

	public void showCreateSolution() throws Exception {
		WizardController wizard = (WizardController) replaceSceneContent(Constancts.WIZARD_PAGE, 1000, 630);
		wizard.setApp(this);
	}

	public void launchSolution(Solution solution) throws Exception {
		LaunchController launch = (LaunchController) replaceSceneContent(Constancts.LAUNCH_PAGE, 1000, 640);
		launch.init(this, solution);
	}

	private OnResizeListener replaceSceneContent(String fxml, int width, int height) throws Exception {
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

		double y = (primaryScreenBounds.getHeight() - height) / 2;
		if (primaryScreenBounds.getHeight() - height > primaryScreenBounds.getHeight() / 4) {
			double tmpY = y / 2;
			if (tmpY > primaryScreenBounds.getMinY()) {
				stage.setY(tmpY);
			} else {
				tmpY = y - y / 3;
				if (tmpY > primaryScreenBounds.getMinY()) {
					stage.setY(tmpY);
				} else {
					stage.setY(y);
				}
			}
		} else {
			stage.setY(y);
		}

		double x = (primaryScreenBounds.getWidth() - width) / 2;
		stage.setX(x + primaryScreenBounds.getMinX());

		FXMLLoader loader = new FXMLLoader();
		loader.setBuilderFactory(new JavaFXBuilderFactory());
		loader.setLocation(Main.class.getResource(fxml));
		Parent root;
		try (InputStream in = Main.class.getResourceAsStream(fxml)) {
			root = (Parent) loader.load(in);
		}

		Scene scene = new Scene(root, width, height);
		stage.setScene(scene);
		stage.sizeToScene();

		final OnResizeListener controller = loader.getController();

		stage.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				controller.onWidthResize(oldValue, newValue);
			}
		});

		stage.heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				controller.onHeightResize(oldValue, newValue);
			}
		});

		controller.initSize(width, height);

		return controller;
	}
}
