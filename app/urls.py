from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import UserRecordViewSet, QuestionnaireViewSet

router = DefaultRouter()
router.register(r'records', UserRecordViewSet)
router.register(r'questionnaire', QuestionnaireViewSet)

urlpatterns = [
    path('', include(router.urls)),
]
