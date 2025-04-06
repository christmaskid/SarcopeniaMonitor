import pickle
import numpy as np
from .models import UserRecord, Questionnaire, PhysicalTest, Prediction

# Load the pre-trained DXA model
DXA_MODEL_PATH = "checkpoints/DXA_best_ridge_model.pkl"
with open(DXA_MODEL_PATH, "rb") as f:
    dxa_model = pickle.load(f)

def predict_asmi(user_record, questionnaire):
    """
    Predict ASMI using the DXA model.
    
    Args:
        user_record (UserRecord): The user record instance.
        questionnaire (Questionnaire): The questionnaire instance.
    
    Returns:
        float: The predicted ASMI value.
    """
    print("Predict ASMI...", flush=True)
    print("input: {}".format(user_record), flush=True)
    print("input: {}".format(questionnaire), flush=True)

    # Calculate age dynamically
    age = questionnaire.calculate_age()

    # Derived variables
    age_weight_interaction = age * user_record.weight
    gender_nutrition_interaction = questionnaire.gender * questionnaire.c15
    body_condition_score = (user_record.bmi * 0.4) + (user_record.weight * 0.3) + (user_record.height * 0.3)

    # Extract required fields in the correct order
    features = [
        age,
        user_record.weight,
        user_record.bmi,
        user_record.height,
        questionnaire.b2_2,
        questionnaire.b1_2,
        user_record.dbp,
        user_record.sbp,
        questionnaire.gender,
        questionnaire.c15,
        questionnaire.a2_1,
        questionnaire.a2_2,
        age_weight_interaction,
        gender_nutrition_interaction,
        body_condition_score,
    ]
    
    # Convert features to a NumPy array and reshape for the model
    features = np.array(features).reshape(1, -1)
    
    # Make prediction
    asmi_prediction = dxa_model.predict(features)[0]

    print("Finish predicting ASMI", flush=True)
    print("ASMI prediction: {}".format(asmi_prediction), flush=True)
    return asmi_prediction

def generate_asmi_prediction():
    """
    Generate and save ASMI prediction for the latest UserRecord and Questionnaire.
    """
    # Check if UserRecord and Questionnaire data exist
    if not UserRecord.objects.exists() or not Questionnaire.objects.exists():
        return  # Do nothing if data is missing

    # Get the latest UserRecord and Questionnaire
    user_record = UserRecord.objects.latest('recordID')
    questionnaire = Questionnaire.objects.latest('id')
    
    # Predict ASMI
    asmi_prediction = predict_asmi(user_record, questionnaire)
    
    # Save the prediction to the Prediction model
    prediction = Prediction.objects.create(
        asmiPrediction=asmi_prediction
    )
    prediction.save()
