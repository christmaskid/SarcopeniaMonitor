import pickle
import numpy as np
import pandas as pd
from .models import PhysicalTest, Prediction, UserRecord, Questionnaire
from .predict_asmi import generate_asmi_prediction
from sklearn.impute import SimpleImputer

# Load models and scaler
WALK_MODEL_PATH = "checkpoints/walk_best_xgb_model.pkl"
STAND_MODEL_PATH = "checkpoints/stand_best_LR_model.pkl"
BOTH_MODEL_PATH = "checkpoints/both_walk_stand_best_xgb_model.pkl"
SCALER_PATH = "checkpoints/sarcopenia4_scaler.pkl"

WALK_THRESHOLD_PATH = "checkpoints/walk_best_threshold.pkl"
STAND_THRESHOLD_PATH = "checkpoints/stand_best_threshold.pkl"
BOTH_THRESHOLD_PATH = "checkpoints/both_walk_stand_best_threshold.pkl"

with open(WALK_MODEL_PATH, "rb") as f:
    walk_model = pickle.load(f)
with open(STAND_MODEL_PATH, "rb") as f:
    stand_model = pickle.load(f)
with open(BOTH_MODEL_PATH, "rb") as f:
    both_model = pickle.load(f)

with open(WALK_THRESHOLD_PATH, "rb") as f:
    walk_threshold = pickle.load(f)
with open(STAND_THRESHOLD_PATH, "rb") as f:
    stand_threshold = pickle.load(f)
with open(BOTH_THRESHOLD_PATH, "rb") as f:
    both_threshold = pickle.load(f)

with open(SCALER_PATH, "rb") as f:
    scaler = pickle.load(f)
imputer = SimpleImputer(strategy="median")

def predict_sarcopenia(user_record, questionnaire, physical_test):
    """
    Predict sarcopenia status using physical test + questionnaire data with Chinese column names.
    """
    print("Predict sarcopenia...", flush=True)

    age = questionnaire.calculate_age()
    walk_data = physical_test.gaitSpeedData
    stand_data = physical_test.standUpData

    if walk_data is None and stand_data is None:
        print("⚠️ No physical test data — fallback to ASMI only", flush=True)
        return None

    if walk_data is not None and stand_data is None:
        print("🦶 Case 2: Only gait speed (walk) is available", flush=True)
        column_names = [
            "5M秒數", "年齡", "身高", "體重", "BMI",
            "臨床衰弱量表等級", "無法啟動做什麼事", "日常活動", "行動能力",
            "性別", "自我評估營養狀況", "吞嚥問題造成體重下降", "抽菸", "自我照顧"
        ]
        values = [[
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
        ]]
        df = pd.DataFrame(values, columns=column_names)
        scaled = scaler.transform(df)
        # Handle missing values by filling with the median (for a single row)
        for column in scaled.columns:
            if scaled[column].isnull().any():
                median_value = scaled[column].median()
                scaled[column].fillna(median_value, inplace=True)
        prediction = walk_model.predict(scaled)[0]
        return int(prediction >= walk_threshold)

    if stand_data is not None and walk_data is None:
        print("🪑 Case 3: Only stand-up test is available", flush=True)
        column_names = [
            "年齡", "坐站5次", "身高", "體重", "BMI",
            "臨床衰弱量表等級", "無法啟動做什麼事", "日常活動", "行動能力",
            "性別", "自我評估營養狀況", "吞嚥問題造成體重下降", "抽菸", "自我照顧"
        ]
        values = [[
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
        ]]
        df = pd.DataFrame(values, columns=column_names)
        scaled = scaler.transform(df)
        # Handle missing values by filling with the median (for a single row)
        for column in scaled.columns:
            if scaled[column].isnull().any():
                median_value = scaled[column].median()
                scaled[column].fillna(median_value, inplace=True)
        prediction = stand_model.predict(scaled)[0]
        return int(prediction >= stand_threshold)

    if walk_data is not None and stand_data is not None:
        print("🏃‍♀️🪑 Case 4: Both gait and stand-up tests are available", flush=True)
        column_names = [
            "5M秒數", "年齡", "坐站5次", "身高", "體重", "BMI",
            "臨床衰弱量表等級", "無法啟動做什麼事", "日常活動", "做事花很大力氣",
            "行動能力", "性別", "自我評估營養狀況", "吞嚥問題造成體重下降", "抽菸", "自我照顧"
        ]
        values = [[
            walk_data,
            age,
            stand_data,
            user_record.height,
            user_record.weight,
            user_record.bmi,
            questionnaire.b9,
            questionnaire.a2_2,
            questionnaire.d3,
            questionnaire.a2_1,
            questionnaire.d1,
            questionnaire.gender,
            questionnaire.c15,
            questionnaire.e1,
            questionnaire.smoking,
            questionnaire.d2,
        ]]
        df = pd.DataFrame(values, columns=column_names)
        scaled = scaler.transform(df)
        # Handle missing values by filling with the median (for a single row)
        for column in scaled.columns:
            if scaled[column].isnull().any():
                median_value = scaled[column].median()
                scaled[column].fillna(median_value, inplace=True)
        prediction = both_model.predict(scaled)[0]
        return int(prediction >= both_threshold)

def generate_sarcopenia_prediction():
    if not UserRecord.objects.exists() or not Questionnaire.objects.exists() or not PhysicalTest.objects.exists():
        return

    user_record = UserRecord.objects.latest('recordID')
    questionnaire = Questionnaire.objects.latest('id')
    physical_test = PhysicalTest.objects.latest('id')

    sarcopenia_status = predict_sarcopenia(user_record, questionnaire, physical_test)

    prediction = Prediction.objects.create(
        sarcopeniaStatus=sarcopenia_status
    )
    prediction.save()
