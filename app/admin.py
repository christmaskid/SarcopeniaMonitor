from django.contrib import admin
from .models import UserRecord, Questionnaire  # Import your models

admin.site.register(UserRecord)  # Register your model in the admin panel
admin.site.register(Questionnaire)