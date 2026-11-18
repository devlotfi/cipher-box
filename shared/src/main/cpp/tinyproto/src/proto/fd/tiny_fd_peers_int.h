/*
    Copyright 2019-2025 (C) Alexey Dynda

    This file is part of Tiny Protocol Library.

    GNU General Public License Usage

    Protocol Library is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Protocol Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Protocol Library.  If not, see <http://www.gnu.org/licenses/>.

    Commercial License Usage

    Licensees holding valid commercial Tiny Protocol licenses may use this file in
    accordance with the commercial license agreement provided in accordance with
    the terms contained in a written agreement between you and Alexey Dynda.
    For further information contact via email on github account.
*/

#pragma once

#include "tiny_fd.h"
#include "tiny_fd_int.h"
#include "tiny_fd_defines_int.h"

static uint8_t inline __is_primary_address(uint8_t address)
{
    address &= ~(HDLC_CR_BIT);
    return address == ( HDLC_PRIMARY_ADDR | HDLC_E_BIT );
}

///////////////////////////////////////////////////////////////////////////////

static uint8_t inline __is_primary_station(tiny_fd_handle_t handle)
{
    return __is_primary_address(handle->addr);
}

///////////////////////////////////////////////////////////////////////////////


static uint8_t inline __is_secondary_station(tiny_fd_handle_t handle)
{
    return !__is_primary_station( handle );
}

///////////////////////////////////////////////////////////////////////////////

static uint8_t inline __peer_to_address_field(tiny_fd_handle_t handle, uint8_t peer)
{
    return handle->peers[peer].addr & (~HDLC_CR_BIT);
}

///////////////////////////////////////////////////////////////////////////////

uint8_t __address_field_to_peer(tiny_fd_handle_t handle, uint8_t address);
uint8_t __switch_to_next_peer(tiny_fd_handle_t handle);
