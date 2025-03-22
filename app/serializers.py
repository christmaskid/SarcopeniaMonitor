from rest_framework import serializers
from .models import UserRecord, RECORD_FIELDS, Questionnaire

class UserRecordSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserRecord
        fields = '__all__'  # Include all fields

class QuesionnaireSerializer(serializers.ModelSerializer):
    class Meta:
        model = Questionnaire
        fields = '__all__'  # Include all fields