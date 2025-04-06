import pickle
import numpy as np
from .models import PhysicalTest, Prediction, UserRecord, Questionnaire
from .predict_asmi import predict_asmi
from .predict_sarcopenia import predict_sarcopenia

def generate_full_prediction():
    """
    Generate and save a full prediction (ASMI + sarcopenia) in a single Prediction object.
    """
    if not UserRecord.objects.exists() or not Questionnaire.objects.exists():
        return

    user_record = UserRecord.objects.latest('recordID')
    questionnaire = Questionnaire.objects.latest('id')

    # Get ASMI
    asmi_prediction = predict_asmi(user_record, questionnaire)

    # Get sarcopenia only if PhysicalTest exists
    sarcopenia_status = None
    if PhysicalTest.objects.exists():
        physical_test = PhysicalTest.objects.latest('id')
        sarcopenia_status = predict_sarcopenia(user_record, questionnaire, physical_test)

    # Save both predictions in one object
    prediction = Prediction.objects.create(
        asmiPrediction=asmi_prediction,
        sarcopeniaStatus=sarcopenia_status
    )
    prediction.save()
