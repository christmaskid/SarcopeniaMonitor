from rest_framework import status, viewsets
from rest_framework.response import Response
from rest_framework.decorators import action
from .models import UserRecord, RECORD_FIELDS, Questionnaire
from .serializers import UserRecordSerializer, QuesionnaireSerializer

class UserRecordViewSet(viewsets.ModelViewSet):
    queryset = UserRecord.objects.all()
    serializer_class = UserRecordSerializer
    lookup_field = 'pk'
    # permission_classes = [permissions.IsAuthenticated]

    def create(self, request, *args, **kwargs):
        print("Create", request.data)  # Log the incoming data
        serializer = self.get_serializer(data=request.data)
        if not serializer.is_valid():
            print("Error", serializer.errors)  # Log validation errors
        serializer.is_valid(raise_exception=True)
        
        validated_data = serializer.validated_data
        print(validated_data)
        for key in RECORD_FIELDS:
            if key in validated_data:
                print(f"+'{key}': {validated_data[key]}")

        # validated_data['timestamp'] = \
        #     f"{validated_data['recordDate']} {validated_data['recordTime']}"
        
        self.perform_create(serializer)
        headers = self.get_success_headers(serializer.data)
        return Response(serializer.data, status=status.HTTP_201_CREATED, headers=headers)

    # def update(self, request, *args, **kwargs):
    #     print("Update Request", request.data)
    #     return super().update(request, *args, **kwargs)

    def update(self, request, *args, **kwargs):
        print("Update Request", request.data)
        partial = kwargs.pop('partial', False)
        instance = self.get_object()
        serializer = self.get_serializer(instance, data=request.data, partial=partial)

        if serializer.is_valid():
            serializer.save()  # 🔥 Ensure changes are saved to the database
            return Response(serializer.data)
        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        

        
class QuestionnaireViewSet(viewsets.ModelViewSet):
    queryset = Questionnaire.objects.all()
    serializer_class = QuesionnaireSerializer
    lookup_field = 'pk'
    # permission_classes = [permissions.IsAuthenticated]
    
    def create(self, request, *args, **kwargs):
        print("Create", request.data['answers'])  # Log the incoming data
        serializer = self.get_serializer(data=request.data['answers'])
        if not serializer.is_valid():
            print("Error", serializer.errors)  # Log validation errors
        serializer.is_valid(raise_exception=True)
        
        validated_data = serializer.validated_data
        print(validated_data)
        for key in RECORD_FIELDS:
            if key in validated_data:
                print(f"+'{key}': {validated_data[key]}")
        
        self.perform_create(serializer)
        headers = self.get_success_headers(serializer.data)
        return Response(serializer.data, status=status.HTTP_201_CREATED, headers=headers)

    @action(detail=False, methods=['get'])
    def latest(self, request):
        latest_entry = Questionnaire.objects.order_by('-timestamp').first()
        print("Latest", latest_entry)
        if latest_entry:
            serializer = self.get_serializer(latest_entry)
            for key in serializer.data:
                print(f"-'{key}': {serializer.data[key]}")
            return Response(serializer.data, status=status.HTTP_200_OK)
        return Response({"error": "No questionnaire found"}, status=status.HTTP_404_NOT_FOUND)