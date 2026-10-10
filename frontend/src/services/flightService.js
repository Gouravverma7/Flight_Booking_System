import api from "./api";

export async function searchFlights(searchRequest) {
    const response = await api.post(
        "/api/flights/search", searchRequest
    )
    return response.data;
}
