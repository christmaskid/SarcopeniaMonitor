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
    # Extract required fields
    features = [
        user_record.age,
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
    ]
    
    # Convert features to a NumPy array and reshape for the model
    features = np.array(features).reshape(1, -1)
    
    # Make prediction
    asmi_prediction = dxa_model.predict(features)[0]
    return asmi_prediction

def generate_asmi_prediction():
    """
    Generate and save ASMI prediction for the latest UserRecord and Questionnaire.
    """
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
