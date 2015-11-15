/*
   Hebcal - A Jewish Calendar Generator
   Copyright (C) 1994  Danny Sadinoff
   Portions Copyright (c) 2002 Michael J. Radwin. All Rights Reserved.
   This file copyright (c) 2003-2013 Aaron Peromsik.
   
   https://github.com/hebcal/hebcal

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

   Danny Sadinoff can be reached at
   1 Cove La.
   Great Neck, NY
   11024

   sadinoff@pobox.com
 */
/***********************************************************************
hebcal wrapper to create a string.
***********************************************************************/

#include "hebcal.h"
#include <stdarg.h>
#include <string.h>
#include <stdlib.h>

////////////////////////////////////////////////////////////////////////
// Globals


static char printf_data[2048];
static int printf_offset = 0;
static int printf_size = 2048;

static short hcMonth, hcDay, hcYear;
static date_t greg_day;
static int date_set = TRUE;

extern int hca_week_zemanim = 0;

// printf redirector
int hca_printf(const char *fmt, ...)
{
  int i;
  va_list args;
  if (printf_data == NULL || printf_size-printf_offset < 100)
     return 0;
  va_start(args, fmt);
  i = vsprintf(printf_data + printf_offset, fmt, args);
  va_end(args);
  printf_offset += strlen(printf_data + printf_offset);
  return i;
}

void hca_printf_setup() {
   printf_offset = 0;
}

void hca_printf_clean() {
   if (printf_data != NULL)
      free(printf_data);
   printf_offset = printf_size = 0;
}

void hca_date_to_ints() {
   hcMonth = greg_day.mm;
   hcYear  = greg_day.yy;
   hcDay   = greg_day.dd;
}

void hca_ints_to_date() {
   greg_day.yy = hcYear;
   greg_day.mm = hcMonth;
   greg_day.dd = hcDay;
}

void hca_set_date (int mm, int dd, int yy)
{
  greg_day.mm = mm;
  greg_day.dd = dd;
  greg_day.yy = yy;
  date_set = TRUE;
}

// redo the text field based on greg_day
char *hca_compute(int num_days)
{
    int zemanim;
   static int here_yet = 0;
   if (!here_yet)
   {
     //      hcp_apply_prefs();
      here_yet = 1;
      localize_to_city("Pawtucket");
      ashkenazis_sw = 1;
      // sunsetAlways_sw = 1;
      // sunriseAlways_sw = 1;
      // dafYomi_sw = 1;
   }
   hca_printf_setup();

    if (num_days == 1)
        zemanim = (ZMAN_SUNRISE | ZMAN_SZKS | ZMAN_TEFILAH | ZMAN_CHATZOT |
                           ZMAN_MINCHA_GEDOLA | ZMAN_MINCHA_KETANA |
                           ZMAN_PLAG_HAMINCHA | ZMAN_SUNSET | ZMAN_TZAIT_42);
    else
        zemanim = hca_week_zemanim;

   if (hebcal_for_range(greg_day, num_days, zemanim))
   {
     //     return "silly stuff";
     return printf_data;
   }
}


char *hca_for_mode(int mode) {
  if (date_set == FALSE)
    setDate(&greg_day);
  hca_date_to_ints();
  printf_sw = 1;
  return hca_compute(1 + 6 * mode);
}

void hca_compute_events(int days) {
  if (date_set == FALSE)
    setDate(&greg_day);
  hca_date_to_ints();
  printf_sw = 0;
  return hca_compute(days);
}

void hca_set_dialect(char *d) {
  
  switch (d[0]) {
  case 'h':
  iso8859_8_sw = TRUE;
    ashkenazis_sw = FALSE;
    break;
  case 's': 
    ashkenazis_sw = FALSE;
  iso8859_8_sw = FALSE;
    break;
  default:
  case 'a':
    ashkenazis_sw = TRUE;
  iso8859_8_sw = FALSE;
  break;
  }


}
