from django.shortcuts import render
from rest_framework import viewsets, status
from rest_framework.response import Response
from rest_framework.decorators import action
from .models import UserRecord, Questionnaire, PhysicalTest, Prediction
from .serializers import UserRecordSerializer, QuestionnaireSerializer, PhysicalTestSerializer, PredictionSerializer

class UserRecordViewSet(viewsets.ModelViewSet):
    queryset = UserRecord.objects.all()
    serializer_class = UserRecordSerializer
    lookup_field = 'recordID'  # Use recordID as the primary key


class QuestionnaireViewSet(viewsets.ModelViewSet):
    queryset = Questionnaire.objects.all()
    serializer_class = QuestionnaireSerializer

    @action(detail=False, methods=['get'], url_path='latest')
    def latest(self, request):
        latest_entry = Questionnaire.objects.order_by('-id').first()
        if latest_entry:
            serializer = self.get_serializer(latest_entry)
            # Apply value mapping here if needed
            mapped_data = self.map_values(serializer.data)
            return Response(mapped_data, status=status.HTTP_200_OK)
        return Response({"error": "No questionnaire found"}, status=status.HTTP_404_NOT_FOUND)

    def map_values(self, data):
        # Example mapping logic
        mapping = {
            "gender": {0: "Male", 1: "Female"},
            "smoking": {0: "Non-smoker", 1: "Smoker", 2: "Ex-smoker"},
        }
        for key, value_map in mapping.items():
            if key in data and data[key] in value_map:
                data[key] = value_map[data[key]]
        return data


class PhysicalTestViewSet(viewsets.ModelViewSet):
    queryset = PhysicalTest.objects.all()
    serializer_class = PhysicalTestSerializer


class PredictionViewSet(viewsets.ModelViewSet):
    queryset = Prediction.objects.all()
    serializer_class = PredictionSerializer

    @action(detail=False, methods=['get'], url_path='latest')
    def latest(self, request):
        latest_entry = Prediction.objects.order_by('-id').first()
        if latest_entry:
            serializer = self.get_serializer(latest_entry)
            return Response(serializer.data, status=status.HTTP_200_OK)
        return Response({"error": "No prediction found"}, status=status.HTTP_404_NOT_FOUND)
