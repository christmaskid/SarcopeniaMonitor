from rest_framework import status, viewsets
from rest_framework.response import Response
from .models import UserRecord, RECORD_FIELDS
from .serializers import UserRecordSerializer

class UserRecordViewSet(viewsets.ModelViewSet):
    queryset = UserRecord.objects.all()
    serializer_class = UserRecordSerializer
    lookup_field = 'recordID'
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

    def update(self, request, *args, **kwargs):
        print("Update Request", request.data)
        return super().update(request, *args, **kwargs)

    # def update(self, request, record_id):
    #     print("Update Request", request.data)
    #     try:
    #         record = UserRecord.objects.get(pk=record_id)
    #     except UserRecord.DoesNotExist:
    #         return Response({"error": "Record not found"}, status=404)

    #     serializer = self.get_serializer(record, data=request.data, partial=True)
        
    #     if serializer.is_valid():
    #         serializer.save()
    #         return Response(serializer.data)
    #     else:
    #         return Response(serializer.errors, status=400)