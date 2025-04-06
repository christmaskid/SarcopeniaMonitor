from django.contrib import admin
from .models import UserRecord, Questionnaire, PhysicalTest, Prediction  # Import your models

admin.site.register(UserRecord)  # Register your model in the admin panel
admin.site.register(Questionnaire)
admin.site.register(PhysicalTest)
admin.site.register(Prediction)