# Generated by Django 5.1.7 on 2025-04-05 14:37

from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ("app", "0004_rename_meal_images_userrecord_mealimages_and_more"),
    ]

    operations = [
        migrations.RenameField(
            model_name="mealimage",
            old_name="meal_type",
            new_name="mealType",
        ),
        migrations.RenameField(
            model_name="mealimage",
            old_name="uploaded_at",
            new_name="uploadedAt",
        ),
    ]
