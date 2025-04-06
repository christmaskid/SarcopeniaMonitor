"""
WSGI config for sarcopeniaMonitor project.

It exposes the WSGI callable as a module-level variable named ``application``.

For more information on this file, see
https://docs.djangoproject.com/en/5.1/howto/deployment/wsgi/
"""

import os

from django.core.wsgi import get_wsgi_application
import sys

sys.path.append('/app')  # or the path to your project root
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'sarcopeniaMonitor.settings')

application = get_wsgi_application()
