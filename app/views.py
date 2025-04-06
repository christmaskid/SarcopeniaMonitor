from django.shortcuts import render
from rest_framework import viewsets, status
from rest_framework.response import Response
from rest_framework.decorators import action
from django.http import JsonResponse
from .models import UserRecord, Questionnaire, PhysicalTest, Prediction
from .serializers import UserRecordSerializer, QuestionnaireSerializer, PhysicalTestSerializer, PredictionSerializer
from rest_framework.parsers import MultiPartParser, FormParser
from rest_framework.decorators import api_view, parser_classes
from django.core.files.storage import default_storage
from django.core.files.base import ContentFile
# from .predict_asmi import generate_asmi_prediction
# from .predict_sarcopenia import generate_sarcopenia_prediction
from .predict_all import generate_full_prediction

class UserRecordViewSet(viewsets.ModelViewSet):
    queryset = UserRecord.objects.all()
    serializer_class = UserRecordSerializer
    lookup_field = 'recordID'  # Use recordID as the primary key

    def update(self, request, *args, **kwargs):
        """
        Override the update method to handle meal images directly in the UserRecord.
        """
        print("Update Request Triggered", flush=True)  # Debug log to confirm method is called
        try:
            print("Request data:", request.data, flush=True)  # Log the incoming data
            partial = kwargs.pop('partial', False)
            instance = self.get_object()
            meal_images_data = request.data.pop('meal_images', [])
            serializer = self.get_serializer(instance, data=request.data, partial=partial)
            serializer.is_valid(raise_exception=True)
            self.perform_update(serializer)

            # Update meal images in the UserRecord
            if meal_images_data:
                instance.meal_images = meal_images_data
                instance.save()

            return Response(serializer.data)
        except Exception as e:
            # Log detailed error information
            print("Error during PUT method for UserRecord:", flush=True)
            print("Request data:", request.data, flush=True)
            print("Error details:", str(e), flush=True)
            return JsonResponse({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

    def retrieve(self, request, *args, **kwargs):
        """
        Override the retrieve method to add error logging when fetching a specific UserRecord.
        """
        try:
            instance = self.get_object()
            serializer = self.get_serializer(instance, context={'request': request})  # Pass request context
            print("Retrieved UserRecord:", serializer.data, flush=True)
            return Response(serializer.data, status=status.HTTP_200_OK)
        except Exception as e:
            # Log the error and return a detailed response
            error_message = f"Error retrieving UserRecord with ID {kwargs.get('recordID')}: {str(e)}"
            print(error_message, flush=True)
            return JsonResponse({"error": error_message}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

    def list(self, request, *args, **kwargs):
        """
        Override the list method to add error logging when fetching all UserRecords.
        """
        try:
            queryset = self.filter_queryset(self.get_queryset())
            serializer = self.get_serializer(queryset, many=True, context={'request': request})  # Pass request context
            print("Retrieved UserRecords List:", serializer.data, flush=True)
            return Response(serializer.data, status=status.HTTP_200_OK)
        except Exception as e:
            # Log the error and return a detailed response
            error_message = f"Error retrieving UserRecords list: {str(e)}"
            print(error_message, flush=True)
            return JsonResponse({"error": error_message}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

    @action(detail=False, methods=['post'], url_path='upload-meal-image', parser_classes=[MultiPartParser, FormParser])
    def upload_meal_image(self, request):
        """
        Handle the upload of meal images.
        """
        try:
            image_file = request.FILES.get('image')

            if not image_file:
                return Response({"error": "No image file provided"}, status=status.HTTP_400_BAD_REQUEST)

            # Save the image to the media directory
            file_path = default_storage.save(f"meal_images/{image_file.name}", ContentFile(image_file.read()))
            image_url = default_storage.url(file_path)

            # Return the image URL
            return Response({"image_uri": image_url}, status=status.HTTP_201_CREATED)
        except Exception as e:
            print("Error uploading meal image:", str(e), flush=True)
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


class QuestionnaireViewSet(viewsets.ModelViewSet):
    queryset = Questionnaire.objects.all()
    serializer_class = QuestionnaireSerializer

    mapping = {
            "birthDate": lambda x: x.replace("/", "-") if isinstance(x, str) else None,  # Ensure date is in string format
            "gender": {0: 1, 1: 0, 2: 0.5}, # Female, Male, Not applicable
            "smoking": {0: 0, 1: 1, 2: 2}, # Never, Sometimes, Frequent
            "a2_1": {0:0, 1:1, 2:2, 3:3},
            "a2_2": {0:0, 1:1, 2:2, 3:3},
            "c15": {0:0, 1:1, 2:2},
            "d1": {0:1, 1:2, 2:3},
            "d2": {0:1, 1:2, 2:3},
            "d3": {0:1, 1:2, 2:3},
            "e1": {0:0, 1:1, 2:2, 3:3, 4:4},
            "meal_status": lambda x: eval(x)+1 if isinstance(x, str) \
                            else x+1 if isinstance(x, int) else None,
        }
    invert_mapping = {
            "birthDate": lambda x: x.replace("-", "/") if isinstance(x, str) else None,
            "meal_status": lambda x: str(x-1),
        }
    for k, v in mapping.items():
        if not callable(v):
            invert_mapping[k] = {vv:int(kk) for kk, vv in v.items()}
    
    eval_keys = ["b1_2", "b2_2", "b9"]

    def create(self, request, *args, **kwargs):
        """
        Override the post method to map input values to real values and add logging for debugging.
        """
        print("Received POST request for Questionnaire", flush=True)
        print("Request data:", request.data, flush=True)

        # Map input values to real values
        mapped_data = self.map_input_values(request.data['answers'])
        print("Mapped data:", mapped_data, flush=True)

        serializer = self.get_serializer(data=mapped_data)
        if not serializer.is_valid():
            print("Error", serializer.errors, flush=True)  # Log validation errors
        serializer.is_valid(raise_exception=True)

        self.perform_create(serializer)
        headers = self.get_success_headers(serializer.data)
        response = Response(serializer.data, status=status.HTTP_201_CREATED, headers=headers)
        print("Response data:", response.data, flush=True)

        return response

    def map_input_values(self, data):
        """
        Map input values to real values for the Questionnaire model.
        """
        for key, value_map in self.mapping.items():
            if key in data:
                if callable(value_map):  # If the mapping is a function
                    data[key] = value_map(data[key])
                elif data[key] in value_map:  # If the mapping is a dictionary
                    data[key] = value_map[data[key]]
        
        for key in self.eval_keys:
            if key in data:
                data[key] = eval(data[key])

        return data

    @action(detail=False, methods=['get'], url_path='latest')
    def latest(self, request):
        latest_entry = Questionnaire.objects.order_by('-id').first()
        
        if latest_entry:
            serializer = self.get_serializer(latest_entry)
            # Apply value mapping here if needed
            mapped_data = self.invert_map_values(serializer.data)
            wrapped_mapped_data = {'answers': mapped_data}
            print("questionnaire", wrapped_mapped_data, flush=True)
            return Response(wrapped_mapped_data, status=status.HTTP_200_OK)

        return Response({"error": "No questionnaire found"}, status=status.HTTP_404_NOT_FOUND)

    def invert_map_values(self, data):
        for key, value_map in self.invert_mapping.items():
            if key in data:
                if callable(value_map):  # If the mapping is a function
                    data[key] = value_map(data[key])
                elif data[key] in value_map:  # If the mapping is a dictionary
                    data[key] = value_map[data[key]]
        
        for key in self.eval_keys:
            if key in data:
                data[key] = str(data[key])

        return data



class PhysicalTestViewSet(viewsets.ModelViewSet):
    queryset = PhysicalTest.objects.all()
    serializer_class = PhysicalTestSerializer


class PredictionViewSet(viewsets.ModelViewSet):
    queryset = Prediction.objects.all()
    serializer_class = PredictionSerializer

    @action(detail=False, methods=['get'], url_path='latest')
    def latest(self, request):
        # Trigger ASMI and sarcopenia predictions before fetching the latest prediction
        # generate_asmi_prediction()
        # generate_sarcopenia_prediction()
        generate_full_prediction()
        latest_entry = Prediction.objects.order_by('-id').first()
        if latest_entry:
            serializer = self.get_serializer(latest_entry)
            print("Latest prediction:", serializer.data, flush=True)
            return Response(serializer.data, status=status.HTTP_200_OK)
        return Response({"error": "No prediction found"}, status=status.HTTP_404_NOT_FOUND)