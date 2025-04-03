import pickle
import numpy as np
from .models import PhysicalTest, Prediction
from .predict_asmi import generate_asmi_prediction

# Load the pre-trained models and thresholds
WALK_THRESHOLD_PATH = "checkpoints/walk_best_threshold.pkl"
STAND_THRESHOLD_PATH = "checkpoints/stand_best_threshold.pkl"
BOTH_MODEL_PATH = "checkpoints/both_walk_stand_xgb_model.pkl"

with open(WALK_THRESHOLD_PATH, "rb") as f:
    walk_threshold = pickle.load(f)

with open(STAND_THRESHOLD_PATH, "rb") as f:
    stand_threshold = pickle.load(f)

with open(BOTH_MODEL_PATH, "rb") as f:
    both_model = pickle.load(f)

def predict_sarcopenia(physical_test):
    """
    Predict sarcopenia status based on physical test inputs.

    Args:
        physical_test (PhysicalTest): The physical test instance.

    Returns:
        int: The predicted sarcopenia status (0 or 1).
    """
    # Extract physical test data
    walk_data = physical_test.gaitSpeedData
    stand_data = physical_test.standUpData

    # Case 1: No physical test data, fallback to ASMI prediction
    if walk_data is None and stand_data is None:
        generate_asmi_prediction()
        return None  # No sarcopenia status prediction

    # Case 2: Only "walk" data is available
    if walk_data is not None and stand_data is None:
        return 1 if walk_data >= walk_threshold else 0

    # Case 3: Only "stand" data is available
    if stand_data is not None and walk_data is None:
        return 1 if stand_data >= stand_threshold else 0

    # Case 4: Both "walk" and "stand" data are available
    if walk_data is not None and stand_data is not None:
        features = np.array([walk_data, stand_data]).reshape(1, -1)
        return both_model.predict(features)[0]

def generate_sarcopenia_prediction():
    """
    Generate and save sarcopenia prediction for the latest PhysicalTest.
    """
    # Get the latest PhysicalTest
    physical_test = PhysicalTest.objects.latest('id')

    # Predict sarcopenia status
    sarcopenia_status = predict_sarcopenia(physical_test)

    # Save the prediction to the Prediction model
    prediction = Prediction.objects.create(
        sarcopeniaStatus=sarcopenia_status
    )
    prediction.save()
