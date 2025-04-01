using Newtonsoft.Json;

namespace NfcAimeReaderDLL
{
    public class CardRequest
    {
        [JsonProperty("module")]
        public string Module { get; set; } = string.Empty;

        [JsonProperty("function")]
        public string Function { get; set; } = string.Empty;

        [JsonProperty("params")]
        public string Params { get; set; } = string.Empty;
    }

    public class CardResponse
    {
        [JsonProperty("status")]
        public bool Status { get; set; }

        [JsonProperty("message")]
        public string Message { get; set; } = string.Empty;
    }
}