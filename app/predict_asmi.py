import pickle
import pandas as pd
import numpy as np
from .models import UserRecord, Questionnaire, PhysicalTest, Prediction
from sklearn.impute import SimpleImputer

# Load the pre-trained DXA model
DXA_MODEL_PATH = "checkpoints/DXA_best_ridge_model.pkl"
SCALER_PATH = "checkpoints/asmi_scaler.pkl"

with open(DXA_MODEL_PATH, "rb") as f:
    dxa_model = pickle.load(f)

with open(SCALER_PATH, "rb") as f:
    scaler = pickle.load(f)

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

    # Create DataFrame for input features
    column_names = [
        "年齡",
        "體重",
        "BMI",
        "身高",
        "過去7天中等費力活動一天幾分鐘",
        "過去7天費力活動一天幾分鐘",
        "舒張壓",
        "收縮壓",
        "性別",
        "自我評估營養狀況",
        "做事花很大力氣",
        "無法啟動做什麼事",
        "age_weight_interaction", "gender_nutrition_interaction", "body_condition_score"
    ]

    values = [[
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
        body_condition_score
    ]]

    input_df = pd.DataFrame(values, columns=column_names)
    print("Input DataFrame (before scaling):\n", input_df, flush=True)

    # Scale the input
    scaled_features = pd.DataFrame(scaler.transform(input_df))
    print("Scaled features:\n", scaled_features, flush=True)
    # Handle missing values by filling with the median (for a single row)
    for column in scaled_features.columns:
        if scaled_features[column].isnull().any():
            median_value = scaled_features[column].median()
            scaled_features[column].fillna(median_value, inplace=True)
    print("Scaled features:\n", scaled_features, flush=True)
    
    # Make prediction
    asmi_prediction = dxa_model.predict(scaled_features)[0]

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
