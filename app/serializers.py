from rest_framework import serializers
from .models import UserRecord, RECORD_FIELDS

class UserRecordSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserRecord
        fields = '__all__'  # Include all fields
