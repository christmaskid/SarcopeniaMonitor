from django.contrib import admin
from .models import UserRecord  # Import your models

admin.site.register(UserRecord)  # Register your model in the admin panel
