import pickle
import numpy as np
from .models import PhysicalTest, Prediction, UserRecord, Questionnaire
from .predict_asmi import generate_asmi_prediction

# Load the pre-trained models and thresholds
WALK_THRESHOLD_PATH = "checkpoints/walk_best_xgb_model.pkl"
STAND_THRESHOLD_PATH = "checkpoints/stand_best_xgb_model.pkl"
BOTH_MODEL_PATH = "checkpoints/both_walk_stand_best_xgb_model.pkl"

with open(WALK_THRESHOLD_PATH, "rb") as f:
    walk_threshold = pickle.load(f)

with open(STAND_THRESHOLD_PATH, "rb") as f:
    stand_threshold = pickle.load(f)

with open(BOTH_MODEL_PATH, "rb") as f:
    both_model = pickle.load(f)

# Load the pre-trained thresholds
WALK_THRESHOLD_PATH = "checkpoints/walk_best_threshold.pkl"
STAND_THRESHOLD_PATH = "checkpoints/stand_best_threshold.pkl"
BOTH_THRESHOLD_PATH = "checkpoints/both_walk_stand_best_threshold.pkl"

with open(WALK_THRESHOLD_PATH, "rb") as f:
    walk_threshold = pickle.load(f)

with open(STAND_THRESHOLD_PATH, "rb") as f:
    stand_threshold = pickle.load(f)

with open(BOTH_THRESHOLD_PATH, "rb") as f:
    both_threshold = pickle.load(f)

def predict_sarcopenia(user_record, questionnaire, physical_test):
    """
    Predict sarcopenia status based on inputs from UserRecord, Questionnaire, and PhysicalTest.

    Args:
        user_record (UserRecord): The user record instance.
        questionnaire (Questionnaire): The questionnaire instance.
        physical_test (PhysicalTest): The physical test instance.

    Returns:
        int: The predicted sarcopenia status (0 or 1).
    """
    print("Predict sarcopenia...", flush=True)
    print("input: {}".format(user_record), flush=True)
    print("input: {}".format(questionnaire), flush=True)
    print("input: {}".format(physical_test), flush=True)

    # Calculate age dynamically
    age = questionnaire.calculate_age()

    # Extract physical test data
    walk_data = physical_test.gaitSpeedData
    stand_data = physical_test.standUpData

    # Case 1: No physical test data, fallback to ASMI prediction
    print("Case 1: No physical test data, fallback to ASMI prediction", flush=True)
    if walk_data is None and stand_data is None:
        generate_asmi_prediction()
        return None  # No sarcopenia status prediction

    # Case 2: Only "walk" data is available
    print("Case 2: Only 'walk' data is available", flush=True)
    if walk_data is not None and stand_data is None:
        features = [
            walk_data,
            age,
            user_record.height,
            user_record.weight,
            user_record.bmi,
            questionnaire.b9,
            questionnaire.a2_2,
            questionnaire.d3,
            questionnaire.d1,
            questionnaire.gender,
            questionnaire.c15,
            questionnaire.e1,
            questionnaire.smoking,
            questionnaire.d2,
        ]
        features = np.array(features).reshape(1, -1)
        prediction = both_model.predict(features)[0]
        print("Prediction: {}".format(prediction), flush=True)
        print("Threshold: {}".format(walk_threshold), flush=True)
        return 1 if prediction >= walk_threshold else 0

    # Case 3: Only "stand" data is available
    print("Case 3: Only 'stand' data is available", flush=True)
    if stand_data is not None and walk_data is None:
        features = [
            age,
            stand_data,
            user_record.height,
            user_record.weight,
            user_record.bmi,
            questionnaire.b9,
            questionnaire.a2_2,
            questionnaire.d3,
            questionnaire.d1,
            questionnaire.gender,
            questionnaire.c15,
            questionnaire.e1,
            questionnaire.smoking,
            questionnaire.d2,
        ]
        features = np.array(features).reshape(1, -1)
        prediction = both_model.predict(features)[0]
        print("Prediction: {}".format(prediction), flush=True)
        print("Threshold: {}".format(stand_threshold), flush=True)
        return 1 if prediction >= stand_threshold else 0

    # Case 4: Both "walk" and "stand" data are available
    print("Case 4: Both 'walk' and 'stand' data are available", flush=True)
    if walk_data is not None and stand_data is not None:
        features = [
            walk_data,
            age,
            stand_data,
            user_record.height,
            user_record.weight,
            user_record.bmi,
            questionnaire.b9,
            questionnaire.a2_2,
            questionnaire.d3,
            questionnaire.d1,
            questionnaire.gender,
            questionnaire.c15,
            questionnaire.e1,
            questionnaire.smoking,
            questionnaire.d2,
        ]
        features = np.array(features).reshape(1, -1)
        prediction = both_model.predict(features)[0]
        print("Prediction: {}".format(prediction), flush=True)
        print("Threshold: {}".format(both_threshold), flush=True)
        return 1 if prediction >= both_threshold else 0

def generate_sarcopenia_prediction():
    """
    Generate and save sarcopenia prediction for the latest UserRecord, Questionnaire, and PhysicalTest.
    """
    # Check if UserRecord, Questionnaire, and PhysicalTest data exist
    if not UserRecord.objects.exists() or not Questionnaire.objects.exists() or not PhysicalTest.objects.exists():
        return  # Do nothing if data is missing

    # Get the latest UserRecord, Questionnaire, and PhysicalTest
    user_record = UserRecord.objects.latest('recordID')
    questionnaire = Questionnaire.objects.latest('id')
    physical_test = PhysicalTest.objects.latest('id')

    # Predict sarcopenia status
    sarcopenia_status = predict_sarcopenia(user_record, questionnaire, physical_test)

    # Save the prediction to the Prediction model
    prediction = Prediction.objects.create(
        sarcopeniaStatus=sarcopenia_status
    )
    prediction.save()
