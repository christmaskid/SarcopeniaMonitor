from rest_framework import serializers
from .models import UserRecord, Questionnaire, PhysicalTest, Prediction, MealImage

class MealImageSerializer(serializers.ModelSerializer):
    class Meta:
        model = MealImage
        fields = ['id', 'image', 'uploaded_at']

class UserRecordSerializer(serializers.ModelSerializer):
    meal_images = MealImageSerializer(many=True, read_only=True)

    class Meta:
        model = UserRecord
        fields = '__all__'  # Include all fields

class QuestionnaireSerializer(serializers.ModelSerializer):
    class Meta:
        model = Questionnaire
        fields = '__all__'  # Include all fields

class PhysicalTestSerializer(serializers.ModelSerializer):
    class Meta:
        model = PhysicalTest
        fields = '__all__'  # Include all fields

class PredictionSerializer(serializers.ModelSerializer):
    class Meta:
        model = Prediction
        fields = '__all__'  # Include all fields
