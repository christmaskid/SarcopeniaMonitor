from rest_framework import serializers
from .models import UserRecord, Questionnaire, PhysicalTest, Prediction

class UserRecordSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserRecord
        fields = '__all__'

class QuestionnaireSerializer(serializers.ModelSerializer):
    class Meta:
        model = Questionnaire
        fields = '__all__'

class PhysicalTestSerializer(serializers.ModelSerializer):
    class Meta:
        model = PhysicalTest
        fields = '__all__'

class PredictionSerializer(serializers.ModelSerializer):
    class Meta:
        model = Prediction
        fields = '__all__'
