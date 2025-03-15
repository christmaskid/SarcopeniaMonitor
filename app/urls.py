from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import UserRecordViewSet

router = DefaultRouter()
router.register(r'records', UserRecordViewSet)

urlpatterns = [
    path('', include(router.urls)),
]
