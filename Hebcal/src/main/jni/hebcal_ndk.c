#include <net_peromsik_hebcal_HebcalNativeLoader.h>
#include <hebcal.h>

extern void hebcal_set_date(int,int,int);

JNIEXPORT void JNICALL Java_net_peromsik_hebcal_HebcalNativeLoader_hebcal_1set_1date
(JNIEnv * env, jobject obj, int mm, int dd, int yy) {
  hca_set_date(mm, dd, yy);
}

extern HebcalEvent events[];
extern int num_events;

JNIEXPORT void JNICALL Java_net_peromsik_hebcal_HebcalNativeLoader_hebcal_1range_1events (JNIEnv *env, jobject obj, jint num_days) {
  int i;
  jclass myclass = (*env)->GetObjectClass(env, obj);
  jmethodID mid_setEvent = (*env)->GetMethodID(env, myclass, "setEvent", 
                                             "(IIIIIIIILjava/lang/String;)V");

  char buffer[1024];

  hca_compute_events(num_days);

  // and fill them in.
  for (i = 0; i < num_events; i ++)
    {
      char *desc = events[i].desc;
      int hours = events[i].time.hours;
      if (events[i].time.pm)
        hours += 12;

      if (iso8859_8_sw)
        {
          int i, j, heb = FALSE;
          for (i=0, j=0; desc[i]; i ++) 
            {
              if (desc[i] < 0x80) 
                {
                  if (heb && isdigit(desc[i]))
                    {
                      heb = FALSE;
                      // buffer[j++] = 0x20;
                      // buffer[j++] = 0x0e;
                    }
                  buffer[j++] = desc[i];
                }
              else
                {
                  // alef in iso8859-8 is 0xe0
                  // alef in utf8 is 0xd7 0x90
                  buffer[j++] = 0xd7;
                  buffer[j++] = desc[i] - 0x50; 
                  heb = TRUE;
                }
            }
          buffer[j] = 0x0;
          desc = buffer;
        }

      (*env)->CallVoidMethod(env, obj, mid_setEvent, i, num_events, 
                             events[i].mm, events[i].dd, events[i].yy,
                             hours, events[i].time.minutes,
                             events[i].daf_flag, 
                             (*env)->NewStringUTF(env, desc));
    }

  clean_events();
}

JNIEXPORT void JNICALL Java_net_peromsik_hebcal_HebcalNativeLoader_hebcal_1set_1prefs (JNIEnv *env, jobject jobj, jboolean daf, jboolean sunrise, jboolean sunset) {

  dafYomi_sw = (int)daf;
  sunriseAlways_sw = (int)sunrise;
  sunsetAlways_sw = (int)sunset;
  
}


JNIEXPORT void JNICALL Java_net_peromsik_hebcal_HebcalNativeLoader_hebcal_1get_1cities (JNIEnv *env, jobject obj) {
  int i, num_cities;
  jclass myclass = (*env)->GetObjectClass(env, obj);
  jmethodID mid_setCity = (*env)->GetMethodID(env, myclass, "setCity", 
                                             "(IILjava/lang/String;)V");
  char **cities;
  char *cur_city;

  num_cities = get_city_data(&cur_city, &cities);

  if (cur_city != NULL)
      (*env)->CallVoidMethod(env, obj, mid_setCity, -1, num_cities,
                             (*env)->NewStringUTF(env, cur_city));

  // and fill them in.
  for (i = 0; i < num_cities; i ++)
    {
      (*env)->CallVoidMethod(env, obj, mid_setCity, i, num_cities,
                             (*env)->NewStringUTF(env, cities[i]));
    }

}

JNIEXPORT void JNICALL Java_net_peromsik_hebcal_HebcalNativeLoader_hebcal_1localize_1to_1city (JNIEnv *env, jobject obj, jstring city) {
  jbyte *str = (*env)->GetStringUTFChars(env, city, NULL);
  localize_to_city(str);
  (*env)->ReleaseStringUTFChars(env, city, str);
}

JNIEXPORT void JNICALL Java_net_peromsik_hebcal_HebcalNativeLoader_hebcal_1set_1dialect (JNIEnv *env, jobject obj, jstring dialect) {
  jbyte *str = (*env)->GetStringUTFChars(env, dialect, NULL);
  hca_set_dialect(str);
  (*env)->ReleaseStringUTFChars(env, dialect, str);
}
